package net.foxgenesis.notes;

import java.time.Instant;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import net.foxgenesis.notes.database.Note;
import net.foxgenesis.notes.database.NotesDatabase;
import net.foxgenesis.watame.util.discord.Colors;
import net.foxgenesis.watame.util.discord.DiscordLogger;
import net.foxgenesis.watame.util.discord.Response;
import net.foxgenesis.watame.util.lang.DiscordLocaleMessageSource;

public class NotesListener extends ListenerAdapter {
	private static final Logger logger = LoggerFactory.getLogger("Notes");

	private final DiscordLocaleMessageSource messages;
	private final NotesDatabase database;
	
	@Autowired
	private DiscordLogger discordLogger;

	public NotesListener(DiscordLocaleMessageSource messages, NotesDatabase database, int perPage) {
		Assert.state(perPage > 0 && perPage <= 3, "Property 'notes.per-page' must be in range of 1-3");

		this.messages = Objects.requireNonNull(messages);
		this.database = Objects.requireNonNull(database);
	}

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.isFromGuild() && event.getName().equalsIgnoreCase("notes")) {
			switch (event.getSubcommandName()) {
			case "add" -> {
				Member target = event.getOption("member", OptionMapping::getAsMember);
				String note = event.getOption("note", OptionMapping::getAsString);
				note = MarkdownSanitizer.sanitize(note);

				if (target == null) {
					error(event, "notes.no-target").queue();
					return;
				}

				User user = target.getUser();
				if (user.isBot() || user.isSystem()) {
					error(event, "notes.bot-user").queue();
					return;
				}

				Member member = event.getMember();
				if (!member.isOwner()) {
					if (target.getIdLong() == member.getIdLong()) {
						error(event, "notes.self").queue();
						return;
					}

					if (!member.canInteract(target)) {
						error(event, "notes.no-interact").queue();
						return;
					}
				}

				Note toAdd = new Note();
				toAdd.setGuild(event.getGuild().getIdLong());
				toAdd.setMember(target.getIdLong());
				toAdd.setModerator(member.getIdLong());
				toAdd.setNote(note);

				try {
					logger.info("User {} added note to {} in {}: {}", member, target, event.getGuild(), note);
					Note added = database.save(toAdd);
					Object[] args = { target.getAsMention(), added.formatted() };
					event.replyEmbeds(Response
							.success(messages.getMessage("notes.added", args, event.getUserLocale().toLocale())))
							.setEphemeral(true).queue();

					modlog(target, member, added, true);
				} catch (Exception e) {
					logger.error("Error while adding note to database: {}", e);
					error(event, "watame.interaction.error", e.getMessage()).queue();
				}
			}
			case "remove" -> {
				Member target = event.getOption("member", OptionMapping::getAsMember);
				int id = event.getOption("id", OptionMapping::getAsInt);

				if (id < 0) {
					error(event, "notes.invalid-id").queue();
					return;
				}

				if (target == null) {
					error(event, "notes.no-target").queue();
					return;
				}

				User user = target.getUser();
				if (user.isBot() || user.isSystem()) {
					error(event, "notes.bot-user").queue();
					return;
				}

				Member member = event.getMember();
				if (!member.isOwner()) {
					if (target.getIdLong() == member.getIdLong()) {
						error(event, "notes.self").queue();
						return;
					}

					if (!member.canInteract(target)) {
						error(event, "notes.no-interact").queue();
						return;
					}
				}

				database.findById(id).ifPresentOrElse(note -> {
					try {
						database.delete(note);
						Object[] args = { target.getAsMention(), note.formatted() };
						event.replyEmbeds(
								Response.success(messages.getMessage("notes.removed", args, event.getUserLocale())))
								.setEphemeral(true).queue();
						logger.info("User {} removed note from {} in {}: {}", member, target, event.getGuild(), note);

						modlog(target, member, note, false);
					} catch (Exception e) {
						logger.error("Error while removing note from database: {}", e);
						error(event, "watame.interaction.error", e.getMessage()).queue();
					}
				}, () -> error(event, "notes.not-found").queue());
			}
			case "list" -> sendNotesPage(event, event.getOption("member", OptionMapping::getAsMember));
			}
		}
	}

	private void modlog(Member target, Member moderator, Note note, boolean added) {
		discordLogger.modlog(target.getGuild(), builder -> {
			builder.setColor(added ? Colors.SUCCESS : Colors.NOTICE);
			builder.setLocalizedTitle(added ? "notes.modlog.added" : "notes.modlog.removed");
			builder.addLocalizedField("notes.modlog.member", target.getAsMention(), true);
			builder.addLocalizedField("notes.modlog.moderator", moderator.getAsMention(), true);
			builder.addLocalizedField("notes.modlog.note", note.formatted(), false);
			builder.setTimestamp(Instant.now());
		});
	}

	@Override
	public void onUserContextInteraction(UserContextInteractionEvent event) {
		if (event.isFromGuild()) {
			if (event.getCommandType() == Type.USER && event.getFullCommandName().equalsIgnoreCase("notes"))
				sendNotesPage(event, event.getTargetMember());
		}
	}

	// ===============================================================================================================================

	private void sendNotesPage(GenericCommandInteractionEvent event, Member target) {
		if (target == null) {
			error(event, "notes.no-target").queue();
			return;
		}

		User user = target.getUser();
		if (user.isBot() || user.isSystem()) {
			error(event, "notes.bot-user").queue();
			return;
		}

		new NotesPage(event, database, target, messages);
	}

	private ReplyCallbackAction error(GenericCommandInteractionEvent event, String code, Object... args) {
		return event.replyEmbeds(Response.error(messages.getMessage(code, args, code, event.getUserLocale())))
				.setEphemeral(true);
	}
}

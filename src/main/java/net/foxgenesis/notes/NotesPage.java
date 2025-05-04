package net.foxgenesis.notes;

import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.foxgenesis.notes.database.Note;
import net.foxgenesis.notes.database.NotesDatabase;
import net.foxgenesis.watame.util.discord.Colors;
import net.foxgenesis.watame.util.discord.PageMenu;
import net.foxgenesis.watame.util.discord.Response;

public class NotesPage extends PageMenu<Note> {

	private final MessageSource messages;
	private final NotesDatabase database;
	private final Member target;

	public NotesPage(GenericCommandInteractionEvent event, NotesDatabase database, Member target,
			MessageSource source) {
		super(event, database.findAllByMember(target, PageRequest.of(0, 3, Sort.by("time").descending())));
		this.database = Objects.requireNonNull(database);
		this.target = Objects.requireNonNull(target);
		this.messages = Objects.requireNonNull(source);
		this.sendInitalMessage(event);
	}

	@Override
	protected MessageEmbed createEmbed(Page<Note> page, Locale locale) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Colors.INFO);
		builder.setAuthor(target.getEffectiveName(), null, target.getEffectiveAvatarUrl());
		builder.appendDescription(writeNotes(page));

		Object[] args = { page.getTotalElements(), page.getNumber() + 1, page.getTotalPages() };
		builder.setFooter(messages.getMessage("notes.footer", args, locale));
		return builder.build();
	}

	@Override
	protected Page<Note> getNewPage(Pageable pagable) {
		return database.findAllByMember(target, pagable);
	}

	@Override
	protected MessageEmbed createExpiredEmbed(Locale locale) {
		return Response.error(messages.getMessage("watame.interaction.expired", null, locale));
	}
	
	private String writeNotes(Page<Note> page) {
		StringBuilder sb = new StringBuilder();
		Iterator<Note> iterator = page.iterator();
		while (iterator.hasNext()) {
			Note note = iterator.next();
			sb.append(note.formatted());

			if (iterator.hasNext())
				sb.append("\n\n");
		}
		return sb.toString();
	}

	@Override
	protected MessageEmbed createEmptyPageEmbed(Locale locale) {
		return Response.error(messages.getMessage("notes.empty", null, locale));
	}

}

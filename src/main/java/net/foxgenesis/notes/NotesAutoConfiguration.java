package net.foxgenesis.notes;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.foxgenesis.notes.database.NotesDatabase;
import net.foxgenesis.springJDA.annotation.SpringJDAAutoConfiguration;
import net.foxgenesis.springJDA.provider.GlobalCommandProvider;
import net.foxgenesis.watame.plugin.WatamePlugin;
import net.foxgenesis.watame.util.lang.DiscordLocaleMessageSource;

@EntityScan
@ComponentScan
@EnableJpaRepositories
@SpringJDAAutoConfiguration
@WatamePlugin(id = "notes")
public class NotesAutoConfiguration implements GlobalCommandProvider {

	@Bean
	NotesListener notesListener(DiscordLocaleMessageSource messages, NotesDatabase database,
			@Value("${notes.per-page:3}") int perPage) {
		return new NotesListener(messages, database, perPage);
	}

	public Set<CommandData> getCommandData() {
		DefaultMemberPermissions perms = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS);

		return Set.of(
				// Context
				Commands.user("Notes").setGuildOnly(true).setDefaultPermissions(perms),

				// Slash
				Commands.slash("notes", "Add notes to server members").setGuildOnly(true).setDefaultPermissions(perms)
						.addSubcommands(
								// Add
								new SubcommandData("add", "Add a note to a member")
										.addOption(OptionType.USER, "member", "Member to attach a note to", true)
										.addOptions(new OptionData(OptionType.STRING, "note", "Note to add", true)
												.setRequiredLength(3, 200)),
								// Remove
								new SubcommandData("remove", "Remove a note from a member")
										.addOption(OptionType.USER, "member", "Member to remove a note from", true)
										.addOption(OptionType.INTEGER, "id", "Note ID to delete", true),

								// Get
								new SubcommandData("list", "Get the notes of a member").addOption(OptionType.USER,
										"member", "Member to get notes for", true)

						));
	}
}

package my.plugin;

import java.util.Collection;
import java.util.Set;

import net.foxgenesis.watame.plugin.IEventStore;
import net.foxgenesis.watame.plugin.Plugin;
import net.foxgenesis.watame.plugin.require.CommandProvider;

import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Template outlining typical plugin setup.
 * 
 * @author Ashley
 */
public class PluginTemplate extends Plugin implements CommandProvider {

	/**
	 * No argument constructor specified by
	 * {@link java.util.ServiceLoader.Provider#get Provider.get()}. If you wish to
	 * have a custom constructor, declare a {@code public static Plugin provider()}
	 * method.
	 */
	public PluginTemplate() {
		super();
		// Load configuration settings or initialize constants
	}

	@Override
	public void preInit() {
		// Load required resources
		// Register custom databases
	}

	@Override
	protected void init(IEventStore eventStore) {
		// Register/retrieve plugin properties
		// Load main chunk of program
		// Register event listeners
	}

	@Override
	public void postInit() {
		// Initialization cleanup
		// Unload unneeded resources
	}

	@Override
	public void onReady() {
		// Called after all plugins have finished post initialization and we are
		// connected to discord
	}

	@Override
	public void close() throws Exception {
		// Unload/shutdown all open resources
		// Databases and event listeners will be unloaded automatically
	}

	@Override
	public Collection<CommandData> getCommands() {
		// Register commands
		// If there are no commands to register, remove CommandProvider from
		// plugin declaration
		return Set.of();
	}
}

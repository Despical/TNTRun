package me.despical.tntrun.commands;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.commands.command.*;
import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;

public abstract class AbstractCommand {

	protected final Main plugin;
	protected final ChatManager chatManager;
	protected final FileConfiguration arenaConfig;

	public AbstractCommand(final Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.arenaConfig = ConfigUtils.getConfig(plugin, "arena");
		this.plugin.getCommandFramework().registerCommands(this);
	}

	protected void saveConfig() {
		ConfigUtils.saveConfig(plugin, arenaConfig, "arena");
	}

	public static void registerCommands(final Main plugin) {
		final Class<?>[] commandClasses = new Class[] {AdminCommands.class, PlayerCommands.class, TabCompleter.class};

		for (final var clazz : commandClasses) {
			try {
				clazz.getConstructor(Main.class).newInstance(plugin);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}
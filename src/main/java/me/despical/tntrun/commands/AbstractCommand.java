package me.despical.tntrun.commands;

import me.despical.commons.string.StringMatcher;
import me.despical.tntrun.Main;
import me.despical.tntrun.handlers.ChatManager;

import java.util.stream.Collectors;

public abstract class AbstractCommand {

	protected final Main plugin;
	protected final ChatManager chatManager;

	public AbstractCommand(final Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getCommandFramework().registerCommands(this);
		this.plugin.getCommandFramework().setMatchFunction(arguments -> {
			if (arguments.isArgumentsEmpty()) return false;

			String label = arguments.getLabel(), arg = arguments.getArgument(0);

			var matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", label + " " + matches.get(0).getMatch()));
				return true;
			}

			return false;
		});
	}

	public static void registerCommands(final Main plugin) {
		final Class<?>[] commandClasses = new Class[] {AdminCommands.class, PlayerCommands.class};

		for (final var clazz : commandClasses) {
			try {
				clazz.getConstructor(Main.class).newInstance(plugin);
			} catch (Exception exception) {
				exception.fillInStackTrace();
			}
		}
	}
}
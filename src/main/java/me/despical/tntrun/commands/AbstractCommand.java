/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
				exception.printStackTrace();
			}
		}
	}
}
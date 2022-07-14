/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

import me.despical.commons.util.Collections;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class TabCompletion implements TabCompleter {

	private final Main plugin;

	public TabCompletion(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		List<String> completions = new ArrayList<>(), commands = plugin.getCommandHandler().getSubCommands().stream().map(SubCommand::getName).collect(Collectors.toList());
		String arg = args[0];

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, commands, completions);
		}

		if (args.length == 2) {
			if (arg.equalsIgnoreCase("top")) {
				return Collections.listOf("kills", "deaths", "games_played", "highest_score", "longest_survive", "loses", "wins");
			}

			if (arg.equalsIgnoreCase("stats")) {
				return plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
			}

			if (!commands.contains(arg)) {
				return null;
			}

			List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());
			StringUtil.copyPartialMatches(args[1], arenas, completions);

			arenas.sort(null);
			return arenas;
		}

		completions.sort(null);
		return completions;
	}
}
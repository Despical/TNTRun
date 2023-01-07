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
import me.despical.tntrun.commands.admin.*;
import me.despical.tntrun.commands.player.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2022
 */
public class CommandHandler implements CommandExecutor {

	private final Main plugin;
	private final Set<SubCommand> subCommands;

	public CommandHandler(Main plugin) {
		this.plugin = plugin;
		this.subCommands = new HashSet<>();

		SubCommand[] cmds = {new CreateCommand(), new DeleteCommand(), new EditCommand(), new ListCommand(), new ForceStartCommand(), new StopCommand(),
			new ReloadCommand(), new HelpCommand(), new JoinCommand(), new LeaveCommand(), new RandomJoinCommand(), new StatsCommand(), new TopPlayersCommand()};

		for (SubCommand cmd : cmds) {
			registerSubCommand(cmd);
		}

		Optional.ofNullable(plugin.getCommand("tntrun")).ifPresent(command -> {
			command.setExecutor(this);
			command.setTabCompleter(new TabCompletion(plugin));
		});
	}

	public void registerSubCommand(SubCommand subCommand) {
		subCommands.add(subCommand);
	}

	public Set<SubCommand> getSubCommands() {
		return new HashSet<>(subCommands);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.getChatManager().color("&3This server is running &bTNT Run v" + plugin.getDescription().getVersion() + " &3by&b Despical"));

			if (sender.hasPermission("tntrun.admin")) {
				sender.sendMessage(plugin.getChatManager().color("&3Commands: &b/" + label + " help"));
			}

			return true;
		}

		for (SubCommand subCommand : subCommands) {
			if (subCommand.getName().equalsIgnoreCase(args[0])) {
				if (!subCommand.hasPermission(sender)) {
					sender.sendMessage(plugin.getChatManager().prefixedMessage("commands.no_permission"));
					return true;
				}

				if (subCommand.getSenderType() == 0 && !(sender instanceof Player)) {
					sender.sendMessage(plugin.getChatManager().prefixedMessage("commands.only_by_player"));
					return true;
				}

				if (args.length - 1 >= subCommand.getMinimumArguments()) {
					try {
						subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
					} catch (CommandException exception) {
						sender.sendMessage(plugin.getChatManager().color("&c" + exception.getMessage()));
					}
				} else if (subCommand.getType() == 0) {
					sender.sendMessage(plugin.getChatManager().color("&cUsage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : "")));
				}

				return true;
			}
		}

		List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

		if (!matches.isEmpty()) {
			sender.sendMessage(plugin.getChatManager().message("commands.did_you_mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			return true;
		}

		return true;
	}
}
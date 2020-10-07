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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import me.despical.tntrun.commands.game.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.commonsbox.string.StringMatcher;
import me.despical.tntrun.Main;
import me.despical.tntrun.commands.SubCommand.SenderType;
import me.despical.tntrun.commands.admin.HelpCommand;
import me.despical.tntrun.commands.admin.ListCommand;
import me.despical.tntrun.commands.admin.arena.DeleteCommand;
import me.despical.tntrun.commands.admin.arena.EditCommand;
import me.despical.tntrun.commands.admin.arena.ForceStartCommand;
import me.despical.tntrun.commands.admin.arena.ReloadCommand;
import me.despical.tntrun.commands.admin.arena.StopCommand;
import me.despical.tntrun.commands.exception.CommandException;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class CommandHandler implements CommandExecutor {

	private final List<SubCommand> subCommands;
	private final Main plugin;

	public CommandHandler(Main plugin) {
		this.plugin = plugin;
		subCommands = new ArrayList<>();
		
		registerSubCommand(new CreateCommand());
		registerSubCommand(new EditCommand());
		registerSubCommand(new DeleteCommand());
		registerSubCommand(new ReloadCommand());
		registerSubCommand(new ListCommand());
		registerSubCommand(new HelpCommand());
		registerSubCommand(new ForceStartCommand());
		registerSubCommand(new StopCommand());
		registerSubCommand(new ArenaSelectorCommand());
		registerSubCommand(new JoinCommand());
		registerSubCommand(new RandomJoinCommand());
		registerSubCommand(new LeaveCommand());
		registerSubCommand(new StatsCommand());
		registerSubCommand(new LeaderBoardCommand());

		plugin.getCommand("tntrun").setExecutor(this);
		plugin.getCommand("tntrun").setTabCompleter(new TabCompletion(this));
	}
	
	public void registerSubCommand(SubCommand subCommand) {
		subCommands.add(subCommand);
	}
	
	public List<SubCommand> getSubCommands() {
		return new ArrayList<>(subCommands);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(ChatColor.DARK_AQUA + "This server is running " + ChatColor.AQUA + "TNT Run " + ChatColor.DARK_AQUA + "v" + this.plugin.getDescription().getVersion() + " by " + ChatColor.AQUA + "Despical");

			if (sender.hasPermission("tntrun.admin")) {
				sender.sendMessage(ChatColor.DARK_AQUA + "Commands: " + ChatColor.AQUA + "/" + label + " help");
			}

			return true;
		}

		for (SubCommand subCommand : subCommands) {
			if (subCommand.isValidTrigger(args[0])) {
				if (!subCommand.hasPermission(sender)) {
					sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.No-Permission"));
					return true;
				}

				if (subCommand.getSenderType() == SenderType.PLAYER && !(sender instanceof Player)) {
					sender.sendMessage(plugin.getChatManager().colorMessage("Commands.Only-By-Player"));
					return false;
				}

				if (args.length - 1 >= subCommand.getMinimumArguments()) {
					try {
						subCommand.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
					} catch (CommandException e) {
						sender.sendMessage(ChatColor.RED + e.getMessage());
					}
				} else {
					if (subCommand.getType() == SubCommand.CommandType.GENERIC) {
						sender.sendMessage(ChatColor.RED + "Usage: /" + label + " " + subCommand.getName() + " " + (subCommand.getPossibleArguments().length() > 0 ? subCommand.getPossibleArguments() : ""));
					}
				}

				return true;
			}
		}

		List<StringMatcher.Match> matches = StringMatcher.match(args[0], subCommands.stream().map(SubCommand::getName).collect(Collectors.toList()));

        if (!matches.isEmpty()) {
          sender.sendMessage(plugin.getChatManager().colorMessage("Commands.Did-You-Mean").replace("%command%", label + " " + matches.get(0).getMatch()));
          return true;
        }

        return true;
	}
}
/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.string.StringUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.data.MysqlManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(Main plugin) {
		super(plugin);
	}

	@Command(
		name = "tntrun.join",
		usage = "/tntrun join <arena>",
		senderType = Command.SenderType.PLAYER
	)
	public void joinCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}

		final var arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

		if (arena == null) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}

		plugin.getArenaManager().joinAttempt(user, arena);
	}

	@Command(
		name = "tntrun.randomjoin",
		usage = "/tntrun randomjoin",
		senderType = Command.SenderType.PLAYER
	)
	public void randomJoinCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var arenas = plugin.getArenaRegistry().getArenas().stream().filter(arena -> arena.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING) && arena.getPlayers().size() < arena.getMaximumPlayers()).toList();

		if (!arenas.isEmpty()) {
			var arena = arenas.get(0);

			plugin.getArenaManager().joinAttempt(user, arena);
			return;
		}

		user.sendMessage("player-commands.no-free-arenas");
	}

	@Command(
		name = "tntrun.leave",
		usage = "/tntrun leave",
		senderType = Command.SenderType.PLAYER
	)
	public void leaveCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
		final var arena = user.getArena();

		if (arena == null) {
			user.sendMessage("messages.arena.not-playing");
			return;
		}

		plugin.getArenaManager().leaveAttempt(user, arena);
	}

	@Command(
		name = "tntrun.stats",
		usage = "/tntrun stats [player]",
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(User user, CommandArguments arguments) {
		final Player sender = arguments.getSender();

		if (arguments.isArgumentsEmpty()) {
			chatManager.getStringList("player-commands.stats-command.messages").stream().map(message -> formatStats(message, true, user)).forEach(user::sendRawMessage);
			return;
		}

		arguments.getPlayer(0).ifPresentOrElse(player -> {
			final var targetUser = plugin.getUserManager().getUser(player);
			final var self = sender.equals(player);

			chatManager.getStringList("player-commands.stats-command.messages").stream().map(message -> formatStats(message, self, targetUser)).forEach(user::sendRawMessage);
		}, () -> arguments.sendMessage(chatManager.message("player-commands.no-player-found")));
	}

	private String formatStats(String message, boolean self, User user) {
		message = message.replace("%header%", chatManager.message("player-commands.stats-command.header" + (self ? "" : "-other")));
		message = message.replace("%player%", user.getName());
		message = message.replace("%coins%", StatsStorage.StatisticType.COINS.from(user));
		message = message.replace("%longest_survive%", StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)));
		message = message.replace("%games_played%", StatsStorage.StatisticType.GAMES_PLAYED.from(user));
		message = message.replace("%wins%", StatsStorage.StatisticType.WINS.from(user));
		message = message.replace("%loses%", StatsStorage.StatisticType.LOSES.from(user));
		return chatManager.rawMessage(message);
	}

	@Command(
		name = "tntrun.top",
		usage = "/tntrun top <statistic name>"
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.message("player-commands.statistics.type-name"));
			return;
		}

		try {
			printLeaderboard(arguments.getSender(), StatsStorage.StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH)));
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.message("player-commands.statistics.invalid-name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		sender.sendMessage(chatManager.message("player-commands.statistics.header"));

		final var stats = StatsStorage.getStats(statisticType);
		final var statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));

		for (var i = 0; i < 10; i++) {
			try {
				var current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				var current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

				if (plugin.getUserManager().getUserDatabase() instanceof MysqlManager mysqlManager) {
					try (final var connection = plugin.getMysqlDatabase().getConnection()) {
						var statement = connection.createStatement();
						var set = statement.executeQuery("SELECT name FROM %s WHERE UUID='%s'".formatted(mysqlManager.getTable(), current.toString()));

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {}
				}

				sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		String message = chatManager.message("player-commands.statistics.format");

		message = message.replace("%position%", Integer.toString(position));
		message = message.replace("%name%", playerName);
		message = message.replace("%value%", statisticName.startsWith("Longest") ? StringFormatUtils.formatIntoMMSS(value) : Integer.toString(value));
		message = message.replace("%statistic%", statisticName);
		return message;
	}
}
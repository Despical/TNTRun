package me.despical.tntrun.commands.command;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.string.StringMatcher;
import me.despical.commons.string.StringUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.AbstractCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class PlayerCommands extends AbstractCommand {

	public PlayerCommands(Main plugin) {
		super(plugin);

		plugin.getCommandFramework().setMatchFunction(arguments -> {
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

	@Command(
		name = "tntrun.join",
		senderType = PLAYER
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
		senderType = PLAYER
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
		senderType = PLAYER
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
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(CommandArguments arguments) {
		final Player sender = arguments.getSender(), player = !arguments.isArgumentsEmpty() ? plugin.getServer().getPlayer(arguments.getArgument(0)) : sender;

		if (player == null) {
			arguments.sendMessage(chatManager.message("player-commands.no-player-found"));
			return;
		}

		final var user = plugin.getUserManager().getUser(player);
		final var path = "player-commands.stats-command.";

		if (player.equals(sender)) {
			sender.sendMessage(chatManager.message(path + "header", user));
		} else {
			sender.sendMessage(chatManager.message(path + "header-other", user));
		}

		sender.sendMessage(chatManager.message(path + "wins", user) + user.getStat(StatsStorage.StatisticType.WINS));
		sender.sendMessage(chatManager.message(path + "loses", user) + user.getStat(StatsStorage.StatisticType.LOSES));
		sender.sendMessage(chatManager.message(path + "coins", user) + user.getStat(StatsStorage.StatisticType.COINS));
		sender.sendMessage(chatManager.message(path + "games-played", user) + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
		sender.sendMessage(chatManager.message(path + "longest-survive", user) + StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)));
		sender.sendMessage(chatManager.message(path + "footer", user));
	}

	@Command(
		name = "tntrun.top"
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

				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (final var connection = plugin.getMysqlDatabase().getConnection()) {
						var statement = connection.createStatement();
						var set = statement.executeQuery("SELECT name FROM playerstats WHERE UUID='" + current.toString() + "'");

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
		message = message.replace("%value%", Integer.toString(value));
		message = message.replace("%statistic%", statisticName);
		return message;
	}
}
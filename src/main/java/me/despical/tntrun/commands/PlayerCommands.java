package me.despical.tntrun.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.string.StringMatcher;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.data.MysqlManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2022
 */
public class PlayerCommands {

	private final Main plugin;
	private final ChatManager chatManager;

	public PlayerCommands(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();

		plugin.getCommandFramework().registerCommands(this);
		plugin.getCommandFramework().setAnyMatch(arguments -> {
			if (arguments.isArgumentsEmpty()) return;

			String label = arguments.getLabel();
			List<StringMatcher.Match> matches = StringMatcher.match(arguments.getArgument(0), plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + '.', "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.message("commands.did-you-mean").replace("%command%", label + " " + matches.get(0).getMatch()));
			}
		});
	}

	@Command(
		name = "tntrun"
	)
	public void tntRunCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.color("&3This server is running &bTNT Run &3v" + plugin.getDescription().getVersion() + " by &bDespical"));
			arguments.sendMessage(chatManager.color("&3Thank you for purchasing our plugin and supporting us!"));

			if (arguments.hasPermission("tntrun.admin")) {
				arguments.sendMessage(chatManager.color("&3Commands: &b/" + arguments.getLabel() + " help"));
			}
		}
	}

	@Command(
		name = "tntrun.join",
		senderType = Command.SenderType.PLAYER
	)
	public void joinCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.type-arena-name"));
			return;
		}

		final Arena arena = ArenaRegistry.getArena(arguments.getArgument(0));

		if (arena != null) {
			ArenaManager.joinAttempt(arguments.getSender(), arena);
			return;
		}

		arguments.sendMessage(chatManager.prefixedMessage("commands.no-arena-like-that"));
	}

	@Command(
		name = "tntrun.leave",
		senderType = Command.SenderType.PLAYER
	)
	public void leaveCommand(CommandArguments arguments) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_LEAVE_COMMAND)) {
			Player player = arguments.getSender();
			Arena arena = ArenaRegistry.getArena(player);

			if (arena == null) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.not-playing", player));
				return;
			}

			player.sendMessage(chatManager.prefixedMessage("commands.teleported-to-the-lobby", player));

			ArenaManager.leaveAttempt(player, arena, "Leave command");
		}
	}

	@Command(
		name = "tntrun.randomjoin",
		senderType = Command.SenderType.PLAYER
	)
	public void randomJoinCommand(CommandArguments arguments) {
		Map<Arena, Integer> arenas = ArenaRegistry.getArenas().stream().filter(arena -> arena.getArenaState() == ArenaState.STARTING && arena.getPlayers().size() < arena.getMaximumPlayers()).collect(Collectors.toMap(arena -> arena, arena -> arena.getPlayers().size(), (a, b) -> b));

		if (!arenas.isEmpty()) {
			Stream<Map.Entry<Arena, Integer>> sorted = arenas.entrySet().stream().sorted(Map.Entry.comparingByValue());
			Arena arena = sorted.findFirst().get().getKey();

			if (arena != null) {
				ArenaManager.joinAttempt(arguments.getSender(), arena);
				return;
			}
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			if ((arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) && arena.getPlayers().size() < arena.getMaximumPlayers()) {
				ArenaManager.joinAttempt(arguments.getSender(), arena);
				return;
			}
		}

		arguments.sendMessage(plugin.getChatManager().prefixedMessage("commands.no-free-arenas"));
	}

	@Command(
		name = "tntrun.stats",
		senderType = Command.SenderType.PLAYER
	)
	public void statsCommand(CommandArguments arguments) {
		final Player sender = arguments.getSender(), target = !arguments.isArgumentsEmpty() ? plugin.getServer().getPlayer(arguments.getArgument(0)) : sender;
		final String path = "commands.stats-command.";

		if (target == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.admin-commands.player-not-found"));
			return;
		}

		if (target.equals(sender)) {
			sender.sendMessage(chatManager.message(path + "header", target));
		} else {
			sender.sendMessage(chatManager.message(path + "header-other", target).replace("%player%", target.getName()));
		}

		final User user = plugin.getUserManager().getUser(target);

		sender.sendMessage(chatManager.message(path + "wins", target) + user.getStat(StatsStorage.StatisticType.WINS));
		sender.sendMessage(chatManager.message(path + "loses", target) + user.getStat(StatsStorage.StatisticType.LOSES));
		sender.sendMessage(chatManager.message(path + "games-played", target) + user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
		sender.sendMessage(chatManager.message(path + "coins", target) + user.getStat(StatsStorage.StatisticType.COINS));
		sender.sendMessage(chatManager.message(path + "longest-survive", target) + StringFormatUtils.formatIntoMMSS(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE)));
		sender.sendMessage(chatManager.message(path + "footer", target));
	}

	@Command(
		name = "tntrun.leaderboard",
		senderType = Command.SenderType.PLAYER
	)
	public void leaderboardCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.type-name"));
			return;
		}

		try {
			StatsStorage.StatisticType statisticType = StatsStorage.StatisticType.valueOf(arguments.getArgument(0).toUpperCase(java.util.Locale.ENGLISH));

			if (!statisticType.isPersistent()) {
				arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.invalid-name"));
				return;
			}

			printLeaderboard(arguments.getSender(), statisticType);
		} catch (IllegalArgumentException exception) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.statistics.invalid-name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		sender.sendMessage(chatManager.message("commands.statistics.header"));

		Map<UUID, Integer> stats = StatsStorage.getStats(statisticType);
		String statistic = StringUtils.capitalize(statisticType.name().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));
		Object[] array = stats.keySet().toArray();
		UUID current = (UUID) array[array.length - 1];

		for (int i = 0; i < 10; i++) {
			try {
				sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.remove(current)));
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = plugin.getMysqlDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery("SELECT name FROM " + ((MysqlManager) plugin.getUserManager().getDatabase()).getTableName() + " WHERE UUID='" + current.toString() + "'");

						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {
						// Ignore exception
					}
				}

				sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		String message = chatManager.message("commands.statistics.format");
		message = StringUtils.replace(message, "%position%", Integer.toString(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", statisticName.equalsIgnoreCase("longest survive") ? StringFormatUtils.formatIntoMMSS(value) : Integer.toString(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}
}
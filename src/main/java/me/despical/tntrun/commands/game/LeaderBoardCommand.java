package me.despical.tntrun.commands.game;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.commands.exception.CommandException;
import me.despical.tntrun.user.data.MysqlManager;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class LeaderBoardCommand extends SubCommand {

	public LeaderBoardCommand(String name) {
		super("top");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) throws CommandException {
		if (args.length == 0) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Statistics.Type-Name"));
			return;
		}
		try {
			StatsStorage.StatisticType statisticType = StatsStorage.StatisticType.valueOf(args[0].toUpperCase(java.util.Locale.ENGLISH));
			if (!statisticType.isPersistent()) {
				sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Statistics.Invalid-Name"));
				return;
			}
			printLeaderboard(sender, statisticType);
		} catch (IllegalArgumentException e) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Statistics.Invalid-Name"));
		}
	}

	private void printLeaderboard(CommandSender sender, StatsStorage.StatisticType statisticType) {
		LinkedHashMap<UUID, Integer> stats = (LinkedHashMap<UUID, Integer>) StatsStorage.getStats(statisticType);
		sender.sendMessage(getPlugin().getChatManager().colorMessage("Commands.Statistics.Header"));
		String statistic = StringUtils.capitalize(statisticType.toString().toLowerCase(java.util.Locale.ENGLISH).replace("_", " "));
		for (int i = 0; i < 10; i++) {
			try {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				sender.sendMessage(formatMessage(statistic, Bukkit.getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
				stats.remove(current);
			} catch (IndexOutOfBoundsException ex) {
				sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
			} catch (NullPointerException ex) {
				UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
				if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
					try (Connection connection = getPlugin().getMysqlDatabase().getConnection()) {
						Statement statement = connection.createStatement();
						ResultSet set = statement.executeQuery("SELECT name FROM " + ((MysqlManager) getPlugin().getUserManager().getDatabase()).getTableName() + " WHERE UUID='" + current.toString() + "'");
						if (set.next()) {
							sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
							continue;
						}
					} catch (SQLException ignored) {
					}
				}
				sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
			}
		}
	}

	private String formatMessage(String statisticName, String playerName, int position, int value) {
		String message = getPlugin().getChatManager().colorMessage("Commands.Statistics.Format");
		message = StringUtils.replace(message, "%position%", String.valueOf(position));
		message = StringUtils.replace(message, "%name%", playerName);
		message = StringUtils.replace(message, "%value%", String.valueOf(value));
		message = StringUtils.replace(message, "%statistic%", statisticName);
		return message;
	}
	
	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}
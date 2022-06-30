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

package me.despical.tntrun.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.user.User;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class MysqlManager implements UserDatabase {

	private final String tableName;
	private final MysqlDatabase database;

	public MysqlManager() {
		this.tableName = ConfigUtils.getConfig(plugin, "mysql").getString("table", "playerstats");
		this.database = plugin.getMysqlDatabase();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + tableName + "` (\n"
					+ "  `UUID` char(36) NOT NULL PRIMARY KEY,\n"
					+ "  `name` varchar(32) NOT NULL,\n"
					+ "  `wins` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `loses` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `longestsurvive` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `gamesplayed` int(11) NOT NULL DEFAULT '0',\n"
					+ "  `coinsearned` int(11) NOT NULL DEFAULT '0'\n" + ");");
			} catch (SQLException exception) {
				exception.printStackTrace();
				LogUtils.sendConsoleMessage("&cCannot save contents to MySQL database!");
				LogUtils.sendConsoleMessage("&cCheck configuration of mysql.yml file or disable mysql option in config.yml");
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			String query = "UPDATE " + tableName + " SET " + stat.getName() + "=" + user.getStat(stat) + " WHERE UUID='" + user.getUniqueId().toString() + "';";

			database.executeUpdate(query);
			LogUtils.log("Executed MySQL: " + query);
		});
	}

	@Override
	public void saveAllStatistic(User user) {
		StringBuilder builder = new StringBuilder(" SET ");

		for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;
			if (builder.toString().equalsIgnoreCase(" SET ")) {
				builder.append(stat.getName()).append("=").append(user.getStat(stat));
			}

			builder.append(", ").append(stat.getName()).append("=").append(user.getStat(stat));
		}

		String update = builder.toString();
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE " + tableName + update + " WHERE UUID='" + user.getPlayer().getUniqueId().toString() + "';"));
	}

	@Override
	public void loadStatistics(User user) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			String uuid = user.getUniqueId().toString(), name = user.getPlayer().getName();

			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT * from " + tableName + " WHERE UUID='" + uuid + "';");

				if (resultSet.next()) {
					LogUtils.log("MySQL Stats | Player {0} already exist. Getting Stats...", name);

					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, resultSet.getInt(stat.getName()));
					}
				} else {
					LogUtils.log("MySQL Stats | Player {0} does not exist. Creating new one...", name);
					statement.executeUpdate("INSERT INTO " + tableName + " (UUID,name) VALUES ('" + uuid + "','" +name + "');");

					for (StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	public String getTableName() {
		return tableName;
	}

	public MysqlDatabase getDatabase() {
		return database;
	}
}
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

package me.despical.tntrun.user.data;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.database.MysqlDatabase;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public non-sealed class MySQLStatistics extends AbstractDatabase {

	private final String tableName;
	private final MysqlDatabase database;

	public MySQLStatistics() {
		this.tableName = ConfigUtils.getConfig(plugin, "mysql").getString("table", "tntrun_stats");
		this.database = new MysqlDatabase(plugin, "mysql");

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();

				statement.executeUpdate("""
					CREATE TABLE IF NOT EXISTS `%s` (
					  `UUID` char(36) NOT NULL PRIMARY KEY,
					  `name` varchar(32) NOT NULL,
					  `wins` int(11) NOT NULL DEFAULT 0,
					  `loses` int(11) NOT NULL DEFAULT 0,
					  `longestsurvive` int(11) NOT NULL DEFAULT 0,
					  `gamesplayed` int(11) NOT NULL DEFAULT 0,
					  `coinsearned` int(11) NOT NULL DEFAULT 0,
					  `spectatornightvision` int(11) NOT NULL DEFAULT 0,
					  `spectatorshowothers` int(11) NOT NULL DEFAULT 1,
					  `spectatorspeed` int(11) NOT NULL DEFAULT 1
					);""".formatted(tableName));
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	@Override
	public void saveStatistic(User user, StatsStorage.StatisticType statisticType) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s SET %s=%d WHERE UUID='%s';".formatted(tableName, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
	}

	@Override
	public void saveStatistics(User user) {
		String update = this.getUpdateStatement(user);

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(tableName, update, user.getUniqueId().toString())));
	}

	@Override
	public void saveAllStatistics() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			User user = plugin.getUserManager().getUser(player);
			String update = this.getUpdateStatement(user);

			database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(tableName, update, user.getUniqueId().toString()));
		}
	}

	@Override
	public void loadStatistics(User user) {
		String uuid = user.getUniqueId().toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (Connection connection = database.getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet result = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(tableName, uuid));

				if (result.next()) {
					for (var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, result.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO %s (UUID,name) VALUES ('%s','%s');".formatted(tableName, uuid, user.getName()));

					for (var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	@Override
	public void shutdown() {
		saveAllStatistics();

		database.shutdownConnPool();
	}

	@NotNull
	public MysqlDatabase getDatabase() {
		return database;
	}

	@NotNull
	public String getTableName() {
		return tableName;
	}

	private String getUpdateStatement(User user) {
		StringBuilder builder = new StringBuilder(" SET ");

		for (var stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			int value = user.getStat(stat);
			String name = stat.getName();

			if (builder.toString().equalsIgnoreCase(" SET ")) {
				builder.append(name).append("=").append(value);
			}

			builder.append(", ").append(name).append("=").append(value);
		}

		return builder.toString();
	}
}
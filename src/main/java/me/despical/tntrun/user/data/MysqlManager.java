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
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public non-sealed class MysqlManager extends IUserDatabase {

	private final String table;

	private MysqlDatabase database;

	public MysqlManager(Main plugin) {
		super(plugin);
		this.table = ConfigUtils.getConfig(plugin, "mysql").getString("table", "tntrun_stats");

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			this.database = plugin.getMysqlDatabase();

			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();

				statement.executeUpdate("""
						CREATE TABLE IF NOT EXISTS `%s` (
						  `UUID` char(36) NOT NULL PRIMARY KEY,
						  `name` varchar(32) NOT NULL,
						  `wins` int(11) NOT NULL DEFAULT '0',
						  `loses` int(11) NOT NULL DEFAULT '0',
						  `longestsurvive` int(11) NOT NULL DEFAULT '0',
						  `gamesplayed` int(11) NOT NULL DEFAULT '1',
						  `coinsearned` int(11) NOT NULL DEFAULT '1',
						  `spectatornightvision` int(11) NOT NULL DEFAULT '0',
						  `spectatorshowothers` int(11) NOT NULL DEFAULT '1',
						  `spectatorspeed` int(11) NOT NULL DEFAULT '1'
						);""".formatted(table));
			} catch (SQLException exception) {
				exception.printStackTrace();

				plugin.getLogger().severe("Couldn't create statistics table on MySQL database!");
			}
		});
	}

	@Override
	public void saveStatistic(@NotNull User user, StatsStorage.StatisticType statisticType) {
		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s SET %s=%d WHERE UUID='%s';".formatted(table, statisticType.getName(), user.getStat(statisticType), user.getUniqueId().toString())));
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		final var builder = new StringBuilder(" SET ");

		for (final var stat : StatsStorage.StatisticType.values()) {
			if (!stat.isPersistent()) continue;

			final var name = stat.getName();
			final var value = user.getStat(stat);

			if (builder.toString().equalsIgnoreCase(" SET ")) {
				builder.append(name).append("=").append(value);
			}

			builder.append(", ").append(name).append("=").append(value);
		}

		final var update = builder.toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> database.executeUpdate("UPDATE %s%s WHERE UUID='%s';".formatted(table, update, user.getUniqueId().toString())));
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		final var uuid = user.getUniqueId().toString();

		plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
			try (final var connection = database.getConnection()) {
				final var statement = connection.createStatement();
				final var result = statement.executeQuery("SELECT * from %s WHERE UUID='%s';".formatted(table, uuid));

				if (result.next()) {
					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, result.getInt(stat.getName()));
					}
				} else {
					statement.executeUpdate("INSERT INTO %s (UUID,name) VALUES ('%s','%s');".formatted(table, uuid, user.getName()));

					for (final var stat : StatsStorage.StatisticType.values()) {
						if (!stat.isPersistent()) continue;

						user.setStat(stat, 0);
					}
				}
			} catch (SQLException exception) {
				exception.printStackTrace();
			}
		});
	}

	@NotNull
	public MysqlDatabase getDatabase() {
		return database;
	}

	public String getTable() {
		return table;
	}
}
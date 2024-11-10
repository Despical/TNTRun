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

package me.despical.tntrun.api.statistic;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.user.data.MySQLStatistics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public final class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private StatsStorage() {
	}

	@NotNull
	@Contract("null -> fail")
	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getUserManager().getUserDatabase() instanceof MySQLStatistics mySQLManager) {
			try (Connection connection = mySQLManager.getDatabase().getConnection()) {
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("SELECT UUID, %s FROM %s ORDER BY %s".formatted(stat.getName(), mySQLManager.getTableName(), stat.getName()));
				Map<UUID, Integer> column = new LinkedHashMap<>();

				while (resultSet.next()) {
					column.put(UUID.fromString(resultSet.getString("UUID")), resultSet.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException exception) {
				exception.printStackTrace();
				return new LinkedHashMap<>();
			}
		}

		FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");
		Map<UUID, Integer> stats = new LinkedHashMap<>();

		for (String string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(Player player, StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}
}
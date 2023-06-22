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

package me.despical.tntrun.api;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.sorter.SortUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class StatsStorage {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	@NotNull
	@Contract("null -> fail")
	public static Map<UUID, Integer> getStats(StatisticType stat) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED)) {
			try (final var connection = plugin.getMysqlDatabase().getConnection()) {
				final var statement = connection.createStatement();
				final var set = statement.executeQuery("SELECT UUID, " + stat.getName() + " FROM playerstats ORDER BY " + stat.getName());
				final var column = new HashMap<UUID, Integer>();

				while (set.next()) {
					column.put(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
				}

				return column;
			} catch (SQLException e) {
				plugin.getLogger().warning("SQLException occured during getting statistics from database!");
				return new HashMap<>();
			}
		}

		final var config = ConfigUtils.getConfig(plugin, "stats");
		final var stats = new HashMap<UUID, Integer>();

		for (var string : config.getKeys(false)) {
			stats.put(UUID.fromString(string), config.getInt(string + "." + stat.getName()));
		}

		return SortUtils.sortByValue(stats);
	}

	public static int getUserStats(final Player player, final StatisticType statisticType) {
		return plugin.getUserManager().getUser(player).getStat(statisticType);
	}

	/**
	 * Available statistics to get.
	 */
	public enum StatisticType {
		COINS("coinsearned", true), GAMES_PLAYED("gamesplayed", true), LOCAL_COINS("local_coins", false),
		LOCAL_DOUBLE_JUMPS("local_double_jumps", false), LOCAL_SURVIVE("local_survive", false), LONGEST_SURVIVE("longestsurvive", true),
		LOSES("loses", true), WINS("wins", true), SPECTATOR_NIGHT_VISION("spectatornightvision", true),
		SPECTATOR_SHOW_OTHERS("spectatorshowothers", true), SPECTATOR_SPEED("spectatorspeed", true);

		final String name;
		final boolean persistent;

		StatisticType(String name, boolean persistent) {
			this.name = name;
			this.persistent = persistent;
		}

		public String getName() {
			return name;
		}

		public boolean isPersistent() {
			return persistent;
		}
	}
}
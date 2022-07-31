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
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.user.User;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class FileStats implements UserDatabase {

	private final FileConfiguration config;

	public FileStats() {
		config = ConfigUtils.getConfig(plugin, "stats");
	}

	@Override
	public void saveStatistic(final User user, final StatsStorage.StatisticType stat) {
		config.set(user.getUniqueId().toString() + "." + stat.getName(), user.getStat(stat));

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveAllStatistic(final User user) {
		final String uuid = user.getUniqueId().toString();

		for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			if (stat.isPersistent()) {
				config.set(uuid + "." + stat.getName(), user.getStat(stat));
			}
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}


	@Override
	public void loadStatistics(final User user) {
		final String uuid = user.getUniqueId().toString();

		for (final StatsStorage.StatisticType stat : StatsStorage.StatisticType.values()) {
			user.setStat(stat, config.getInt(uuid + "." + stat.getName(), 0));
		}
	}
}
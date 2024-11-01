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
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public non-sealed class FlatFileStatistics extends AbstractDatabase {

	private final FileConfiguration config;

	public FlatFileStatistics() {
		this.config = ConfigUtils.getConfig(plugin, "stats");
	}

	@Override
	public void saveStatistic(@NotNull User user, StatisticType statisticType) {
		config.set(user.getUniqueId().toString() + "." + statisticType.getName(), user.getStat(statisticType));

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveStatistics(@NotNull User user) {
		String uuid = user.getUniqueId().toString();

		for (var stat : StatisticType.values()) {
			if (stat.isPersistent()) {
				config.set(uuid + "." + stat.getName(), user.getStat(stat));
			}
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void saveAllStatistics() {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			User user = plugin.getUserManager().getUser(player);
			String uuid = user.getUniqueId().toString();

			for (var stat : StatisticType.values()) {
				if (stat.isPersistent()) {
					config.set(uuid + "." + stat.getName(), user.getStat(stat));
				}
			}
		}

		ConfigUtils.saveConfig(plugin, config, "stats");
	}

	@Override
	public void loadStatistics(@NotNull User user) {
		String uuid = user.getUniqueId().toString();

		for (var stat : StatisticType.values()) {
			user.setStat(stat, config.getInt(uuid + "." + stat.getName()));
		}
	}

	@Override
	public void shutdown() {
		this.saveAllStatistics();
	}
}
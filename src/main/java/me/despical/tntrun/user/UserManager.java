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

package me.despical.tntrun.user;

import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.data.FileStats;
import me.despical.tntrun.user.data.MysqlManager;
import me.despical.tntrun.user.data.UserDatabase;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

	private final Set<User> users;
	private final UserDatabase database;

	public UserManager(Main plugin) {
		this.users = new HashSet<>();
		this.database = plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager(plugin) : new FileStats(plugin);

		plugin.getServer().getOnlinePlayers().forEach(this::getUser);
	}

	public User getUser(Player player) {
		final UUID uuid = player.getUniqueId();

		for (User user : users) {
			if (user.getUniqueId().equals(uuid)) {
				return user;
			}
		}

		LogUtils.log("Registering new user {0} ({1})", uuid, player.getName());

		final User user = new User(player);

		database.loadStatistics(user);
		users.add(user);
		return user;
	}

	public Set<User> getUsers(Arena arena) {
		return arena.getPlayers().stream().map(this::getUser).collect(Collectors.toSet());
	}

	public void saveStatistic(User user, StatsStorage.StatisticType stat) {
		if (!stat.isPersistent()) return;

		database.saveStatistic(user, stat);
	}

	public void saveAllStatistic(User user) {
		database.saveAllStatistic(user);
	}

	public void loadStatistics(User user) {
		database.loadStatistics(user);
	}

	public void removeUser(User user) {
		users.remove(user);
	}

	public UserDatabase getDatabase() {
		return database;
	}
}
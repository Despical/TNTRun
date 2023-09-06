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

package me.despical.tntrun.user;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.user.data.FileStatistics;
import me.despical.tntrun.user.data.MysqlManager;
import me.despical.tntrun.user.data.IUserDatabase;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

	@NotNull
	private final Set<User> users;

	@NotNull
	private final IUserDatabase userDatabase;

	public UserManager(Main plugin) {
		this.users = new HashSet<>();
		this.userDatabase = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MysqlManager(plugin) : new FileStatistics(plugin);

		plugin.getServer().getOnlinePlayers().stream().map(this::getUser).forEach(this::loadStatistics);
	}

	@NotNull
	public User addUser(final Player player) {
		final User user = new User(player);

		this.users.add(user);
		return user;
	}

	public void removeUser(final Player player) {
		this.users.remove(this.getUser(player));
	}

	@NotNull
	public User getUser(final Player player) {
		final UUID uuid = player.getUniqueId();

		for (User user : this.users) {
			if (uuid.equals(user.getUniqueId())) {
				return user;
			}
		}

		return this.addUser(player);
	}

	@NotNull
	public Set<User> getUsers() {
		return Set.copyOf(users);
	}

	@NotNull
	public IUserDatabase getUserDatabase() {
		return this.userDatabase;
	}

	public void saveStatistic(final User user, final StatsStorage.StatisticType statisticType) {
		if (!statisticType.isPersistent()) return;

		this.userDatabase.saveStatistics(user);
	}

	public void saveStatistics(final User user) {
		this.userDatabase.saveStatistics(user);
	}

	public void loadStatistics(final User user) {
		this.userDatabase.loadStatistics(user);
	}
}
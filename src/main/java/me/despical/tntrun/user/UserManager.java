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

package me.despical.tntrun.user;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.user.data.AbstractDatabase;
import me.despical.tntrun.user.data.FlatFileStatistics;
import me.despical.tntrun.user.data.MySQLStatistics;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

    @NotNull
    private final Map<UUID, User> users;

    @NotNull
    private final AbstractDatabase userDatabase;

    public UserManager(Main plugin) {
        this.users = new HashMap<>();
        this.userDatabase = plugin.getOption(ConfigPreferences.Option.DATABASE_ENABLED) ? new MySQLStatistics() : new FlatFileStatistics();
    }

    @NotNull
    public User addUser(Player player) {
        User user = new User(player);
        users.put(player.getUniqueId(), user);

        userDatabase.loadStatistics(user);
        return user;
    }

    public void removeUser(Player player) {
        users.remove(player.getUniqueId());
    }

    @NotNull
    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());

        if (user != null) {
            return user;
        }

        return this.addUser(player);
    }

    @NotNull
    public Set<User> getUsers() {
        return users
            .values()
            .stream()
            .filter(user -> {
                Player player = user.getPlayer();

                return player != null && player.isOnline();
            }).collect(Collectors.toSet());
    }

    @NotNull
    public AbstractDatabase getUserDatabase() {
        return userDatabase;
    }

    public void saveStatistic(User user, StatisticType statisticType) {
        if (!statisticType.isPersistent()) return;

        this.userDatabase.saveStatistics(user);
    }

    public void saveStatistics(User user) {
        this.userDatabase.saveStatistics(user);
    }
}

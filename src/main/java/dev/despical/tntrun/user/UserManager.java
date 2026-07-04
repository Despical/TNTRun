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

package dev.despical.tntrun.user;

import dev.despical.tntrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class UserManager {

    private final Main plugin;
    private final Map<UUID, User> users;

    public UserManager(Main plugin) {
        this.plugin = plugin;
        this.users = new HashMap<>();
        this.loadDataOfOnlinePlayers();
    }

    public User getUser(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return player == null ? null : this.getUser(player);
    }

    public User getUser(Player player) {
        User user = users.get(player.getUniqueId());

        if (user != null) {
            return user;
        }

        return createNewUser(player);
    }

    public void removeUser(User user) {
        users.remove(user.getUUID());
    }

    public Set<User> getUsers() {
        return Set.copyOf(users.values());
    }

    public User createNewUser(Player player) {
        User user = new User(player);
        users.put(player.getUniqueId(), user);

        plugin.getDatabase().loadData(user);
        return user;
    }

    private void loadDataOfOnlinePlayers() {
        Bukkit.getOnlinePlayers().forEach(this::createNewUser);
    }
}

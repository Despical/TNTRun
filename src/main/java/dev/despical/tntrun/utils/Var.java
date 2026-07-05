/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.utils;

import dev.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class Var {

    private static final Var EMPTY_PLAYER = new Var("%player%", "null");

    public final String name;
    public final Object value;

    private Var(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public static Var of(String name, Object value) {
        return new Var(name, value);
    }

    public static Var of(String name, Collection<String> list) {
        return new Var(name, list.isEmpty() ? Utils.NONE : String.join(", ", list));
    }

    public static Var ofPlayer(Player player) {
        if (player == null) {
            return EMPTY_PLAYER;
        }

        return new Var("%player%", player.getDisplayName());
    }

    public static Var ofPlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) {
            return EMPTY_PLAYER;
        }

        return ofPlayer(player);
    }

    public static Var ofPlayer(User user) {
        return new Var("%player%", user.getName());
    }
}

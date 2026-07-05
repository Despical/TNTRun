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

package dev.despical.tntrun.api.event.player;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.event.TNTRunEvent;
import dev.despical.tntrun.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an event related to a specific player in TNTRun.
 * <p>
 * Provides convenient access to the {@link Player} object and the corresponding
 * {@link User} instance used internally by the plugin.
 * <p>
 * Example subclasses and their purposes:
 * <ul>
 *     <li>{@link PlayerJoinAttemptEvent} – fired when a player attempts to join a game (cancellable)</li>
 *     <li>{@link PlayerLeaveGameEvent} – fired when a player leaves a game</li>
 *     <li>{@link PlayerStatisticChangeEvent} – fired when a player's statistic changes</li>
 * </ul>
 * <p>
 * Use {@link #getPlayer()} for Bukkit-level operations, and
 * {@link #getUser()} for plugin-specific data.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 * @since 29.01.2026
 */
@Getter
@AllArgsConstructor
public abstract class PlayerEvent extends TNTRunEvent {

    /** The Bukkit player associated with this event */
    @NotNull
    private final Player player;

    /**
     * Returns the plugin-specific {@link User} for this player.
     *
     * @return the User object representing the player
     */
    @NotNull
    public final User getUser() {
        return Main.getInstance().getUserManager().getUser(player);
    }

    @Override
    public String toString() {
        return "[player=%s]".formatted(player.getName());
    }
}

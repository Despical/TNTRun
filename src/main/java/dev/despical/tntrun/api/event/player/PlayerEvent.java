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
 * Base class for TNTRun events associated with a single Bukkit player.
 * <p>
 * Use {@link #getPlayer()} for Bukkit operations and {@link #getUser()} for
 * TNTRun-specific state such as statistics, spectator state, and arena data.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
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

    /**
     * Returns a compact debug representation containing the player name.
     *
     * @return a string containing the event player
     */
    @Override
    public String toString() {
        return "[player=%s]".formatted(player.getName());
    }
}

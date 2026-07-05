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

import dev.despical.tntrun.game.Game;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Fired when a player attempts to join a TNTRun game.
 * <p>
 * This event is triggered <b>before</b> the player is officially added to the game.
 * Listeners can cancel the join attempt by calling {@link #setCancelled(boolean)}.
 * <p>
 * Use {@link #getPlayer()} to access the Bukkit player, and {@link #getUser()}
 * for plugin-specific user data.
 * <p>
 * The game can be accessed via {@link #getGame()}.
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Prevent a player from joining based on custom conditions</li>
 *     <li>Log join attempts or send custom messages</li>
 * </ul>
 * <p>
 * Lifecycle note: if the event is cancelled, the player will <b>not</b> be added
 * to the game.
 *
 * @author Despical
 * @since 29.01.2026
 */
@Getter
public class PlayerJoinAttemptEvent extends PlayerEvent implements Cancellable {

    /**
     * Whether this join attempt is cancelled.
     * Listeners can set this to true to prevent the player from joining.
     */
    @Setter
    private boolean cancelled;

    /** The game that the player is attempting to join */
    private final Game game;

    public PlayerJoinAttemptEvent(Player player, Game game) {
        super(player);
        this.game = game;
    }
}

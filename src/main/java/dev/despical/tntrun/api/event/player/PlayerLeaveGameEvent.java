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
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called immediately before a player is removed from an active TNTRun game.
 * <p>
 * The player is still registered in the game when the event is fired,
 * and leave cleanup has not run yet. This makes the event useful for reading
 * final in-game state before inventories, visibility, and membership are reset.
 * <ul>
 *   <li>The player is still part of the game</li>
 *   <li>Scores and game state are still available</li>
 *   <li>The leave reason is available through {@code getReason()}</li>
 * </ul>
 * <p>
 * This event is informational and is not cancellable.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
public class PlayerLeaveGameEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The game the player is leaving.
     */
    private final Game game;

    /**
     * The reason why the player left the game.
     */
    private final LeaveReason reason;

    /**
     * Constructs a new PlayerLeaveGameEvent.
     *
     * @param player the player leaving the game
     * @param game the game the player is leaving
     * @param reason the reason the player is leaving
     */
    public PlayerLeaveGameEvent(Player player, Game game, LeaveReason reason) {
        super(player);
        this.game = game;
        this.reason = reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    /**
     * Represents the cause of a player leaving the game.
     */
    public enum LeaveReason {

        /**
         * Player left using the leave command.
         */
        LEAVE_COMMAND,

        /**
         * Player left using a leave item.
         */
        LEAVE_ITEM,

        /**
         * Player got kicked from the game by an administrator.
         */
        KICK,

        /**
         * Player left due to disconnecting from the server.
         */
        DISCONNECT
    }
}

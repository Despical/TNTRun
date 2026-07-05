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

package dev.despical.tntrun.api.event.game;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.StopReason;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Called after a TNTRun game has been forcefully stopped and cleaned up.
 * <p>
 * This event is fired after stop cleanup has run:
 * <ul>
 *     <li>Scoreboards and boss bars have been removed</li>
 *     <li>Players have been restored and teleported to the configured end location</li>
 *     <li>Visibility, inventory, health, food, flight, and potion effects have been reset</li>
 *     <li>The game's active user list has been cleared</li>
 * </ul>
 * <p>
 * Since the active user list is empty at this point, use {@link #getStoppedPlayers()}
 * to inspect the players that were part of the game before cleanup.
 * <p>
 * The game normally transitions into {@link GameState#RESTARTING} after this
 * event unless the server is reloading or shutting down.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
public class GameStopEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * The reason why the game was stopped.
     */
    private final StopReason stopReason;

    /**
     * Immutable snapshot of player UUIDs that were in the game before cleanup.
     */
    private final List<UUID> stoppedPlayers;

    /**
     * Constructs a new GameStopEvent.
     *
     * @param game the game instance
     * @param stopReason the reason for stopping the game
     */
    public GameStopEvent(Game game, StopReason stopReason) {
        this(game, stopReason, List.of());
    }

    /**
     * Constructs a new GameStopEvent.
     *
     * @param game the game instance
     * @param stopReason the reason for stopping the game
     * @param stoppedPlayers players that were in the game before cleanup
     */
    public GameStopEvent(Game game, StopReason stopReason, List<UUID> stoppedPlayers) {
        super(game);
        this.stopReason = stopReason;
        this.stoppedPlayers = List.copyOf(stoppedPlayers);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

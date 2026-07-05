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
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Cancellable;

/**
 * Called immediately before a TNTRun game transitions from one state to another.
 * <p>
 * This event is fired <b>before</b> the actual game state is changed, allowing
 * listeners to inspect the current state and, if needed, cancel the transition.
 * <p>
 * You can use {@link #getOldState()} to get the current state and
 * {@link #getNewState()} to see which state the game is about to enter.
 * <p>
 * Cancelling this event prevents the game from changing its state.
 * Typical use cases include:
 * <ul>
 *     <li>Blocking transitions to certain states under custom conditions</li>
 *     <li>Updating UI or boss bars before a state change</li>
 *     <li>Logging state transitions for debugging or analytics</li>
 * </ul>
 * <p>
 * Note that listeners should avoid performing heavy operations here,
 * as this event is fired synchronously within the state transition.
 *
 * @see Game#setGameState(GameState)
 * @author Despical
 * @since 29.01.2026
 */
@Getter
public class GameStateChangeEvent extends GameEvent implements Cancellable {

    /** Whether this state change has been cancelled */
    @Setter
    private boolean cancelled;

    /** The previous game state before transition */
    private final GameState oldState;

    /** The game state that is about to be entered */
    private final GameState newState;

    /**
     * Constructs a new GameStateChangeEvent.
     *
     * @param game the game instance
     * @param oldState the current state before the transition
     * @param newState the state the game is about to enter
     */
    public GameStateChangeEvent(Game game, GameState oldState, GameState newState) {
        super(game);
        this.oldState = oldState;
        this.newState = newState;
    }
}

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
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called immediately before a TNTRun game changes state.
 * <p>
 * The game is still in the old state when this event is fired. Cancelling the
 * event prevents the new state from being applied, so the target state's first
 * tick will not run.
 * <p>
 * Typical use cases:
 * <ul>
 *     <li>Blocking a transition under custom conditions</li>
 *     <li>Logging state changes for analytics or debugging</li>
 *     <li>Running custom logic before a state handler starts</li>
 * </ul>
 *
 * @see Game#setGameState(GameState)
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
public class GameStateChangeEvent extends GameEvent implements Cancellable {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Whether this state change has been cancelled.
     */
    @Setter
    private boolean cancelled;

    /**
     * The game state before the transition.
     */
    private final GameState oldState;

    /**
     * The game state that will be entered if the event is not cancelled.
     */
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

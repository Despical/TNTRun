package dev.despical.tntrun.api.event.game;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.StopReason;
import lombok.Getter;

/**
 * Called when a TNTRun game has been completely stopped.
 * <p>
 * This event is fired after all post-game cleanup has been performed:
 * <ul>
 *     <li>All scoreboards and boss bars have been removed</li>
 *     <li>Rounds and tasks have been reset</li>
 *     <li>Players have been teleported to the end location, their inventories reset,
 *         health and game mode restored, and potion effects cleared</li>
 *     <li>Visibility settings restored so that players can see each other again</li>
 * </ul>
 * <p>
 * At this point, the game is no longer active, and listeners can safely perform
 * end-of-game logic such as logging, rewarding players, or triggering external integrations.
 * <p>
 * <b>Important:</b> The game will normally transition into the {@link GameState#RESTARTING} state
 * after this event <i>unless</i> the stop reason is <code>SERVER_RELOAD</code> or <code>SERVER_SHUTDOWN</code>,
 * in which case the server may reload or shut down before the restarting phase begins.
 * <p>
 * Possible {@link StopReason} values and their meanings:
 * <ul>
 *     <li>{@link StopReason#STOP_COMMAND}: The game was stopped by an administrator command. The restarting phase will follow.</li>
 *     <li>{@link StopReason#SERVER_RELOAD}: The plugin detected a server reload. The restarting phase may not occur.</li>
 *     <li>{@link StopReason#SERVER_SHUTDOWN}: The server is shutting down. The restarting phase will not occur.</li>
 * </ul>
 *
 * @author Despical
 * @since 29.01.2026
 */
public class GameStopEvent extends GameEvent {

    /**
     * The reason why the game was stopped.
     */
    @Getter
    private final StopReason stopReason;

    /**
     * Constructs a new GameStopEvent.
     *
     * @param game the game instance
     * @param stopReason the reason for stopping the game
     */
    public GameStopEvent(Game game, StopReason stopReason) {
        super(game);
        this.stopReason = stopReason;
    }
}

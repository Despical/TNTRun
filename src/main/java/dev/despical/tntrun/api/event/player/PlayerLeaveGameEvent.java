package dev.despical.tntrun.api.event.player;

import dev.despical.tntrun.game.Game;
import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Called right before a player is removed from an active TNTRun game.
 * <p>
 * This event is fired exactly one step before the actual leave logic
 * is executed. At this point, the player is still fully registered
 * in the game and no cleanup or state changes have been applied yet.
 * <p>
 * The event is triggered regardless of how the player leaves:
 * command usage, leave item, or disconnect.
 * <p>
 * When this event is called:
 * <ul>
 *   <li>The player is still part of the game</li>
 *   <li>Scores, round data, and game state are still intact</li>
 *   <li>No inventory restore or visibility cleanup has run yet</li>
 *   <li>The leave process has not been finalized</li>
 * </ul>
 * <p>
 * This event is intended for observing and reacting to a player leaving,
 * such as logging, statistics tracking, external integrations, or custom
 * side effects.
 * <p>
 * Note: This event is informational and is not cancellable.
 *
 * @author Despical
 * @since 29.01.2026
 */

@Getter
public class PlayerLeaveGameEvent extends PlayerEvent {

    /**
     * The game the player is leaving.
     */
    private final Game game;

    /**
     * The reason why the player left the game.
     */
    private final LeaveReason reason;

    public PlayerLeaveGameEvent(Player player, Game game, LeaveReason reason) {
        super(player);
        this.game = game;
        this.reason = reason;
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

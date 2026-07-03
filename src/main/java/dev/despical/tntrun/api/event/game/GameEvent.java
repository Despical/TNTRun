package dev.despical.tntrun.api.event.game;

import dev.despical.tntrun.api.event.TNTRunEvent;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.bossbar.BossBarManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.messages.MessageTicker;
import dev.despical.tntrun.game.messages.PlacementMessenger;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.game.visibility.VisibilityManager;
import dev.despical.tntrun.scoreboard.ScoreboardManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all events related to an active TNTRun game instance.
 * <p>
 * This event provides access to the {@link Game} object, which represents
 * the current game session and exposes core managers such as:
 * <ul>
 *   <li>{@link Arena} - The arena where the game is running</li>
 *   <li>{@link VisibilityManager} - Controls player visibility rules</li>
 *   <li>{@link ScoreboardManager} - Updates player scoreboards</li>
 *   <li>{@link ScoreRegistry} - Stores and manages player scores</li>
 *   <li>{@link MessageTicker} - Displays timed game messages</li>
 *   <li>{@link PlacementMessenger} - Handles placement-based announcements</li>
 *   <li>{@link BossBarManager} - Manages boss bar displays</li>
 * </ul>
 *
 * @author Despical
 * @since 29.01.2026
 */
@AllArgsConstructor
public abstract class GameEvent extends TNTRunEvent {

    /**
     * The game instance associated with this event.
     */
    @Getter
    protected final Game game;

    /**
     * Returns the arena where this game is running.
     *
     * @return the active {@link Arena}
     */
    @NotNull
    public Arena getArena() {
        return game.getArena();
    }

    @Override
    public String toString() {
        return "[arena=%s]".formatted(game.getArena());
    }
}

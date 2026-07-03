package dev.despical.tntrun.api.event.game;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Called when a TNTRun game has fully ended and entered its ending phase.
 * <p>
 * This event is fired after all rounds are completed, final scores are calculated,
 * and post-game summary logic has been executed. At this point:
 * <ul>
 *   <li>Final scores and placements are finalized</li>
 *   <li>The winner and Top 3 rankings are available</li>
 *   <li>Summary and placement messages may have already been sent</li>
 *   <li>Players have been teleported to the start location</li>
 * </ul>
 * <p>
 * The game may remain in an ending state for a short duration, during which
 * players are still allowed to leave before the server transitions to the
 * restarting phase.
 * <p>
 * This event is intended for rewarding players, persisting statistics,
 * triggering external integrations, or running custom end-game logic.
 *
 * @apiNote Players may leave the game before the ending timer expires.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public class GameEndEvent extends GameEvent {

    public GameEndEvent(Game game) {
        super(game);
    }

    /**
     * Returns the winning user of the game.
     *
     * @return the {@link User} who won the game, or {@code null} if no winner exists
     */
    @Nullable
    public User getWinner() {
        return game.getScores().getWinner();
    }

    /**
     * Returns the Top 3 players and their final scores.
     *
     * @return an immutable map of {@link UUID} to score values
     */
    @NotNull
    public Map<UUID, Integer> getTop3() {
        return game.getScores().getTop3();
    }
}

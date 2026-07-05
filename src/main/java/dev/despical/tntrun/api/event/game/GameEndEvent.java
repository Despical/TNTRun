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
import dev.despical.tntrun.user.User;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Called after a TNTRun game finishes and enters the ending phase.
 * <p>
 * This event is fired after the winner and placements have been calculated,
 * summary messages have been sent, players have been moved back to the start
 * location, statistics have been applied, and arena records have been updated.
 * At this point:
 * <ul>
 *   <li>Final scores and placements are finalized</li>
 *   <li>The winner and top-three rankings are available</li>
 *   <li>Players are still part of the game while the ending timer counts down</li>
 * </ul>
 * <p>
 * This event is useful for rewards, external integrations, post-game logging,
 * or custom end-game effects.
 *
 * @apiNote Players may leave the game before the ending timer expires.
 *
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public class GameEndEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Constructs a new GameEndEvent.
     *
     * @param game the game that has finished
     */
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

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}

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
 * <p>
 * Created at 18.06.2026
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

    /**
     * Returns a compact debug representation containing the related arena.
     *
     * @return a string containing the event arena
     */
    @Override
    public String toString() {
        return "[arena=%s]".formatted(game.getArena());
    }
}

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
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called after a TNTRun game enters active play.
 * <p>
 * At this point players have been moved to the start location, per-game scores
 * have been reset, the survival round has started, block removal has been
 * scheduled, and the start message has been broadcast.
 * <p>
 * This event is informational and is not cancellable.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 * @since 29.01.2026
 */
public class GameStartEvent extends GameEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    /**
     * Constructs a new GameStartEvent.
     *
     * @param game the game that has entered active play
     */
    public GameStartEvent(Game game) {
        super(game);
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

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

/**
 * Called when the game has finished initialization and is about to start.
 * <p>
 * At the time this event is fired, all game setup logic has been completed
 * (players teleported, scores reset, tasks prepared, etc.).
 * <p>
 * The actual game loop will become active on the <b>next server tick</b>,
 * meaning gameplay logic, timers, and round processing will begin immediately after.
 *
 * @author Despical
 * <p>
 * Created at 18.06.2026
 * @since 29.01.2026
 */
public class GameStartEvent extends GameEvent {

    public GameStartEvent(Game game) {
        super(game);
    }
}

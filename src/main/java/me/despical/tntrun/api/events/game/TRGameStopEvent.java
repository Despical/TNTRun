/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import org.bukkit.event.HandlerList;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * <p>
 * Called when arena is stopped.
 */
public class TRGameStopEvent extends TREvent {

	private final HandlerList HANDLERS = new HandlerList();

	public TRGameStopEvent(Arena arena) {
		super(arena);
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
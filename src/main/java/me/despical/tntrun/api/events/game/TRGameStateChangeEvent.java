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

import org.bukkit.event.HandlerList;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * 
 * Called when arena game state has changed.
 */
public class TRGameStateChangeEvent extends TREvent {

	private final HandlerList HANDLERS = new HandlerList();
	private final ArenaState arenaState;

	public TRGameStateChangeEvent(Arena eventArena, ArenaState arenaState) {
		super(eventArena);
		this.arenaState = arenaState;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public ArenaState getArenaState() {
		return arenaState;
	}
}
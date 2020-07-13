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

	private static final HandlerList HANDLERS = new HandlerList();
	private ArenaState arenaState;

	public TRGameStateChangeEvent(Arena eventArena, ArenaState arenaState) {
		super(eventArena);
		this.arenaState = arenaState;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public ArenaState getArenaState() {
		return arenaState;
	}
}
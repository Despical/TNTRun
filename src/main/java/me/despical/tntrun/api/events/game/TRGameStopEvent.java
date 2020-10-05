package me.despical.tntrun.api.events.game;

import org.bukkit.event.HandlerList;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * 
 * Called when arena is stopped.
 */
public class TRGameStopEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();

	public TRGameStopEvent(Arena arena) {
		super(arena);
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
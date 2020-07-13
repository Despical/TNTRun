package me.despical.tntrun.api.events;

import org.bukkit.event.Event;

import me.despical.tntrun.arena.Arena;

/**
 * Represents TNT Run game related events.
 */
public abstract class TREvent extends Event {

	protected Arena arena;

	public TREvent(Arena eventArena) {
		arena = eventArena;
	}

	/**
	 * Returns event arena
	 *
	 * @return event arena
	 */
	public Arena getArena() {
		return arena;
	}
}

package me.despical.tntrun.api.events;

import me.despical.tntrun.arena.Arena;
import org.bukkit.event.Event;

/**
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public abstract class TREvent extends Event {

	protected final Arena arena;

	public TREvent(Arena eventArena) {
		this.arena = eventArena;
	}

	public Arena getArena() {
		return this.arena;
	}
}
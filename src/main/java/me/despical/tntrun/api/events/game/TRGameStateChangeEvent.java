package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public class TRGameStateChangeEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final ArenaState arenaState;

	public TRGameStateChangeEvent(Arena arena, ArenaState arenaState) {
		super(arena);
		this.arenaState = arenaState;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public ArenaState getNewState() {
		return arenaState;
	}
}
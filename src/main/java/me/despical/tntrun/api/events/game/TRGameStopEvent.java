package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public class TRGameStopEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final boolean quickStop;

	public TRGameStopEvent(Arena arena, boolean quickStop) {
		super(arena);
		this.quickStop = quickStop;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public boolean isQuickStop() {
		return quickStop;
	}
}
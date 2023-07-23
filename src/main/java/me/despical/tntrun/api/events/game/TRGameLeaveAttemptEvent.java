package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public class TRGameLeaveAttemptEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;

	public TRGameLeaveAttemptEvent(Player player, Arena arena) {
		super(arena);
		this.player = player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}
}
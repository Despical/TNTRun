package me.despical.tntrun.api.events.game;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;

/**
 * @author Despical
 * @since 1.0.0
 * <p>
 * 
 * Called when player is attempting to leave arena.
 */
public class TRGameLeaveAttemptEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();
	private final Player player;

	public TRGameLeaveAttemptEvent(Player player, Arena targetArena) {
		super(targetArena);
		this.player = player;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Player getPlayer() {
		return player;
	}

	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
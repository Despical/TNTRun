package me.despical.tntrun.api.events.game;

import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 23.07.2023
 */
public class TRGameJoinAttemptEvent extends TREvent implements Cancellable {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private boolean isCancelled;

	public TRGameJoinAttemptEvent(Player player, Arena targetArena) {
		super(targetArena);
		this.player = player;
		this.isCancelled = false;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public boolean isCancelled() {
		return this.isCancelled;
	}

	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	public Player getPlayer() {
		return player;
	}
}
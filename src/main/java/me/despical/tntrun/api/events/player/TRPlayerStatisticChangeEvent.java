package me.despical.tntrun.api.events.player;

import me.despical.tntrun.api.StatsStorage;
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
public class TRPlayerStatisticChangeEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();

	private final Player player;
	private final StatsStorage.StatisticType statisticType;
	private final int number;

	public TRPlayerStatisticChangeEvent(Arena arena, Player player, StatsStorage.StatisticType statisticType, int number) {
		super(arena);
		this.player = player;
		this.statisticType = statisticType;
		this.number = number;
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

	public StatsStorage.StatisticType getStatisticType() {
		return statisticType;
	}

	public int getNumber() {
		return number;
	}
}
package me.despical.tntrun.api.events.player;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.TREvent;
import me.despical.tntrun.arena.Arena;

/**
 * @author Despical
 * @see StatsStorage.StatisticType
 * @since 1.0.0
 * <p>
 * Called when player receive new statistic.
 */
public class TRPlayerStatisticChangeEvent extends TREvent {

	private static final HandlerList HANDLERS = new HandlerList();
	private final Player player;
	private final StatsStorage.StatisticType statisticType;
	private final int number;

	public TRPlayerStatisticChangeEvent(Arena eventArena, Player player, StatsStorage.StatisticType statisticType, int number) {
		super(eventArena);
		this.player = player;
		this.statisticType = statisticType;
		this.number = number;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

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
/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.despical.tntrun.user;

import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.player.TRPlayerStatisticChangeEvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.handlers.items.GameItem;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private boolean spectator;
	private static long cooldownCounter;

	private final Player player;
	private final Map<String, Double> cooldowns;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	public User(Player player) {
		this.player = player;
		this.cooldowns = new HashMap<>();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public Player getPlayer() {
		return player;
	}

	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	public Arena getArena() {
		return ArenaRegistry.getArena(player);
	}

	public boolean isSpectator() {
		return spectator;
	}

	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}

	public int getStat(StatsStorage.StatisticType stat) {
		final Integer statistic = stats.get(stat);

		if (statistic == null) {
			stats.put(stat, 0);
			return 0;
		}

		return statistic;
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new TRPlayerStatisticChangeEvent(getArena(), player, stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}

	public void setCooldown(String s, double seconds) {
		cooldowns.put(s, seconds + cooldownCounter);
	}

	public double getCooldown(String s) {
		final Double cooldown = cooldowns.get(s);

		return (cooldown == null || cooldown <= cooldownCounter) ? 0 : cooldown - cooldownCounter;
	}

	public void addGameItem(final String id) {
		final GameItem gameItem = plugin.getGameItemManager().getGameItem(id);

		if (gameItem == null) return;

		this.player.getInventory().setItem(gameItem.getSlot(), gameItem.getItemStack());
	}

	public void resetTemporaryStats() {
		for (StatsStorage.StatisticType statistic : StatsStorage.StatisticType.values()) {
			if (statistic.isPersistent()) continue;

			setStat(statistic, 0);
		}

		this.spectator = false;
	}

	public static void cooldownHandlerTask() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
	}
}
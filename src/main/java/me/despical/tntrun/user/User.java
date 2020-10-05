package me.despical.tntrun.user;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.ScoreboardManager;

import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.player.TRPlayerStatisticChangeEvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static long cooldownCounter = 0;
	private final ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
	private final Player player;
	private boolean spectator = false;
	private final Map<StatsStorage.StatisticType, Integer> stats = new EnumMap<>(StatsStorage.StatisticType.class);
	private final Map<String, Double> cooldowns = new HashMap<>();

	public User(Player player) {
		this.player = player;
	}
	
	public static void cooldownHandlerTask() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
	}

	public Arena getArena() {
		return ArenaRegistry.getArena(player);
	}

	public Player getPlayer() {
		return player;
	}
	
	public boolean isSpectator() {
		return spectator;
	}
	
	public void setSpectator(boolean b) {
		spectator = b;
	}

	public int getStat(StatsStorage.StatisticType stat) {
		if (!stats.containsKey(stat)) {
			stats.put(stat, 0);

			return 0;
		} else if (stats.get(stat) == null) {
			return 0;
		}

		return stats.get(stat);
	}
	
	public void removeScoreboard() {
		player.setScoreboard(scoreboardManager.getNewScoreboard());
	}

	public void setStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, i);

		Bukkit.getScheduler().runTask(plugin, () -> {
			TRPlayerStatisticChangeEvent playerStatisticChangeEvent = new TRPlayerStatisticChangeEvent(getArena(), player, stat, i);
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}

	public void addStat(StatsStorage.StatisticType stat, int i) {
		stats.put(stat, getStat(stat) + i);

		Bukkit.getScheduler().runTask(plugin, () -> {
			TRPlayerStatisticChangeEvent playerStatisticChangeEvent = new TRPlayerStatisticChangeEvent(getArena(), player, stat, getStat(stat));
			Bukkit.getPluginManager().callEvent(playerStatisticChangeEvent);
		});
	}
	
	public void setCooldown(String s, double seconds) {
		cooldowns.put(s, seconds + cooldownCounter);
	}

	public double getCooldown(String s) {
		return !cooldowns.containsKey(s) || cooldowns.get(s) <= cooldownCounter ? 0 : cooldowns.get(s) - cooldownCounter;
	}
}
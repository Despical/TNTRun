package me.despical.tntrun.handlers;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {

	@Override
	public boolean persist() {
		return true;
	}

	public String getIdentifier() {
		return "tntrun";
	}

	public String getAuthor() {
		return "Despical";
	}

	public String getVersion() {
		return "1.0.4";
	}

	public String onPlaceholderRequest(Player player, String id) {
		if (player == null) {
			return null;
		}

		switch (id.toLowerCase()) {
			case "wins":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.WINS));
			case "loses":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOSES));
			case "games_played":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.GAMES_PLAYED));
			case "longest_survive":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LONGEST_SURVIVE));
			case "coins":
				return String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.COINS));
			default:
				return handleArenaPlaceholderRequest(id);
		}
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) {
			return null;
		}

		String[] data = id.split(":");
		Arena arena = ArenaRegistry.getArena(data[0]);

		if (arena == null) {
			return null;
		}

		switch (data[1].toLowerCase()) {
			case "players":
				return String.valueOf(arena.getPlayers().size());
			case "max_players":
				return String.valueOf(arena.getMaximumPlayers());
			case "min_players":
				return String.valueOf(arena.getMinimumPlayers());
			case "state":
				return String.valueOf(arena.getArenaState());
			case "state_pretty":
				return arena.getArenaState().getFormattedName();
			case "mapname":
				return arena.getMapName();
			default:
				return null;
		}
	}
}
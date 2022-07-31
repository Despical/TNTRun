/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

package me.despical.tntrun.handlers;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PlaceholderManager extends PlaceholderExpansion {

	private final Main plugin;

	public PlaceholderManager(Main plugin) {
		this.plugin = plugin;

		this.register();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@NotNull
	@Override
	public String getIdentifier() {
		return "tntrun";
	}

	@NotNull
	@Override
	public String getAuthor() {
		return "Despical";
	}

	@NotNull
	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public String onPlaceholderRequest(Player player, @NotNull String id) {
		if (player == null) return null;

		User user = plugin.getUserManager().getUser(player);

		switch (id.toLowerCase()) {
			case "wins":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.WINS));
			case "loses":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.LOSES));
			case "games_played":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
			case "longest_survive":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE));
			case "coins":
				return Integer.toString(user.getStat(StatsStorage.StatisticType.COINS));
			default:
				return handleArenaPlaceholderRequest(id);
		}
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) return null;

		final String[] data = id.split(":");
		final Arena arena = ArenaRegistry.getArena(data[0]);

		if (arena == null) return null;

		switch (data[1].toLowerCase()) {
			case "players":
				return Integer.toString(arena.getPlayers().size());
			case "players_left":
				return Integer.toString(arena.getPlayersLeft().size());
			case "max_players":
				return Integer.toString(arena.getMaximumPlayers());
			case "min_players":
				return Integer.toString(arena.getMinimumPlayers());
			case "state":
				return arena.getArenaState().name();
			case "state_pretty":
				return arena.getArenaState().getFormattedName();
			case "map_name":
				return arena.getMapName();
			default:
				return null;
		}
	}
}
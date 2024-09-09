/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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
import me.despical.commons.number.NumberUtils;
import me.despical.commons.util.Collections;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PlaceholderHandler extends PlaceholderExpansion {

	private final Main plugin;

	public PlaceholderHandler(Main plugin) {
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

		if (id.startsWith("leaderboard_")) {
			return this.getLeaderboardEntry(id);
		}

		final var user = plugin.getUserManager().getUser(player);

		return switch (id.toLowerCase()) {
			case "wins" -> Integer.toString(user.getStat(StatsStorage.StatisticType.WINS));
			case "loses" -> Integer.toString(user.getStat(StatsStorage.StatisticType.LOSES));
			case "games_played" -> Integer.toString(user.getStat(StatsStorage.StatisticType.GAMES_PLAYED));
			case "longest_survive" -> Integer.toString(user.getStat(StatsStorage.StatisticType.LONGEST_SURVIVE));
			case "local_coins" -> Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS));
			case "local_survive" -> Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_SURVIVE));
			case "local_double_jumps" -> Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS));
			case "coins" -> Integer.toString(user.getStat(StatsStorage.StatisticType.COINS));
			default -> handleArenaPlaceholderRequest(id);
		};
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) return null;

		final var data = id.split(":");
		final var arena = plugin.getArenaRegistry().getArena(data[0]);

		if (arena == null) return null;

		return switch (data[1].toLowerCase()) {
			case "players" -> Integer.toString(arena.getPlayers().size());
			case "players_left" -> Integer.toString(arena.getPlayersLeft().size());
			case "max_players" -> Integer.toString(arena.getMaximumPlayers());
			case "min_players" -> Integer.toString(arena.getMinimumPlayers());
			case "state" -> arena.getArenaState().name();
			case "state_pretty" -> arena.getArenaState().getFormattedName();
			case "map_name" -> arena.getMapName();
			default -> null;
		};
	}

	private String getLeaderboardEntry(String id) {
		final var split = id.substring(id.lastIndexOf('_') + 1).split(":");
		final var stat = StatsStorage.StatisticType.match(split[0]);

		if (stat == null) {
			return "There is no statistic name called " + id;
		}

		if (!stat.isPersistent()) {
			return "Only the persistent statistics can be viewed.";
		}

		final var stats = Collections.listFromMap(StatsStorage.getStats(stat));
		final int position = stats.size() - NumberUtils.getInt(split[1]);

		if (stats.size() == position) {
			return "Out of Bounds";
		}

		final boolean isValue = split.length == 3 && "value".equals(split[2]);

		if (position < 0) {
			return plugin.getChatManager().message("placeholders.empty-" + (isValue ? "value" : "position"));
		}

		final var entry = stats.get(position);

		return isValue ? Integer.toString(entry.getValue()) : plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
	}
}
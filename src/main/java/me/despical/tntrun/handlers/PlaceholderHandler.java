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
import me.despical.commons.string.StringFormatUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

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

		if (id.startsWith("top:")) {
			return this.handleLeaderboardPlaceholders(id);
		}

		User user = plugin.getUserManager().getUser(player);

		return switch (id) {
			case "wins" -> Integer.toString(user.getStat(StatisticType.WINS));
			case "loses" -> Integer.toString(user.getStat(StatisticType.LOSES));
			case "games_played" -> Integer.toString(user.getStat(StatisticType.GAMES_PLAYED));
			case "longest_survive" -> Integer.toString(user.getStat(StatisticType.LONGEST_SURVIVE));
			case "local_coins" -> Integer.toString(user.getStat(StatisticType.LOCAL_COINS));
			case "local_survive" -> Integer.toString(user.getStat(StatisticType.LOCAL_SURVIVE));
			case "local_double_jumps" -> Integer.toString(user.getStat(StatisticType.LOCAL_DOUBLE_JUMPS));
			case "coins" -> Integer.toString(user.getStat(StatisticType.COINS));
			default -> handleArenaPlaceholderRequest(id);
		};
	}

	private String handleArenaPlaceholderRequest(String id) {
		if (!id.contains(":")) return null;

		final var data = id.split(":");
		final var arena = plugin.getArenaRegistry().getArena(data[0]);

		if (arena == null) return null;

		return switch (data[1]) {
			case "players" -> Integer.toString(arena.getPlayers().size());
			case "players_left" -> Integer.toString(arena.getPlayersLeft().size());
			case "max_players" -> Integer.toString(arena.getMaximumPlayers());
			case "min_players" -> Integer.toString(arena.getMinimumPlayers());
			case "state" -> arena.getArenaState().name();
			case "state_pretty" -> arena.getArenaState().getFormattedName();
			case "map_name" -> arena.getMapName();
			case "timer" -> Integer.toString(arena.getTimer());
			case "timer_pretty" -> StringFormatUtils.formatIntoMMSS(arena.getTimer());
			default -> null;
		};
	}

	private String handleLeaderboardPlaceholders(String id) {
		String[] split = id.split(":");

		if (split.length != 4) {
			return null;
		}

		String statName = split[1];
		StatisticType statisticType = StatisticType.match(statName);

		if (statisticType == null) {
			return "No statistic like that: " + statName;
		}

		int position = NumberUtils.getInt(split[2], 1);
		Map.Entry<UUID, Integer> entry = plugin.getLeaderboardManager().getEntry(statisticType, position);

		boolean isName = "name".equals(split[3]);

		if (entry == null) {
			return plugin.getChatManager().message("placeholders.empty-" + (isName ? "position" : "value"));
		}

		if (isName) {
			return plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
		}

		return Integer.toString(entry.getValue());
	}
}
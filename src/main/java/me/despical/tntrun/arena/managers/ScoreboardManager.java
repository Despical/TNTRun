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

package me.despical.tntrun.arena.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.scoreboard.ScoreboardLib;
import me.despical.commons.scoreboard.common.EntryBuilder;
import me.despical.commons.scoreboard.type.Entry;
import me.despical.commons.scoreboard.type.Scoreboard;
import me.despical.commons.scoreboard.type.ScoreboardHandler;
import me.despical.commons.string.StringFormatUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2029
 */
public class ScoreboardManager {

	private final Main plugin;
	private final Arena arena;
	private final Set<Scoreboard> scoreboards;

	public ScoreboardManager(Main plugin, Arena arena) {
		this.plugin = plugin;
		this.arena = arena;
		this.scoreboards = new HashSet<>();
	}

	public void createScoreboard(Player player) {
		Scoreboard scoreboard = ScoreboardLib.createScoreboard(player).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(Player player) {
				return plugin.getChatManager().message("Scoreboard.Title");
			}

			@Override
			public List<Entry> getEntries(Player player) {
				return formatScoreboard(player);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	public void removeScoreboard(Player player) {
		for (Scoreboard board : scoreboards) {
			if (board.getHolder().equals(player)) {
				scoreboards.remove(board);
				board.deactivate();
				return;
			}
		}
	}

	public void stopAllScoreboards() {
		scoreboards.forEach(Scoreboard::deactivate);
		scoreboards.clear();
	}

	private List<Entry> formatScoreboard(Player player) {
		EntryBuilder builder = new EntryBuilder();
		String path = arena.getArenaState() == ArenaState.IN_GAME || arena.getArenaState() == ArenaState.ENDING ? "Scoreboard.Content.Playing" :  "Scoreboard.Content." + arena.getArenaState().getFormattedName();

		for (String line : plugin.getChatManager().getStringList(path)) {
			builder.next(formatScoreboardLine(line, player));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, Player player) {
		String formattedLine = line;

		formattedLine = StringUtils.replace(formattedLine, "%time%", Integer.toString(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%map_name%", arena.getMapName());
		formattedLine = StringUtils.replace(formattedLine, "%players%", Integer.toString(arena.getPlayers().size()));
		formattedLine = StringUtils.replace(formattedLine, "%max_players%", Integer.toString(arena.getMaximumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%min_players%", Integer.toString(arena.getMinimumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%coins_earned%", Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_COINS)));
		formattedLine = StringUtils.replace(formattedLine, "%double_jumps%", Integer.toString(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS)));
		formattedLine = plugin.getChatManager().color(formattedLine);

		if (plugin.getChatManager().isPapiEnabled()) {
			formattedLine = PlaceholderAPI.setPlaceholders(player, formattedLine);
		}

		return formattedLine;
	}
}
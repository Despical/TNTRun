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
import me.despical.tntrun.user.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2029
 */
public class ScoreboardManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final List<Scoreboard> scoreboards = new ArrayList<>();
	private final Arena arena;

	public ScoreboardManager(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Creates arena scoreboard for target user
	 *
	 * @param user user that represents game player
	 * @see User
	 */
	public void createScoreboard(User user) {
		Scoreboard scoreboard = ScoreboardLib.createScoreboard(user.getPlayer()).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(Player player) {
				return plugin.getChatManager().colorMessage("Scoreboard.Title");
			}

			@Override
			public List<Entry> getEntries(Player player) {
				return formatScoreboard(user);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	/**
	 * Removes scoreboard of user
	 *
	 * @param user user that represents game player
	 * @see User
	 */
	public void removeScoreboard(User user) {
		for (Scoreboard board : scoreboards) {
			if (board.getHolder().equals(user.getPlayer())) {
				scoreboards.remove(board);
				board.deactivate();
				return;
			}
		}
	}

	/**
	 * Forces all scoreboards to deactivate.
	 */
	public void stopAllScoreboards() {
		scoreboards.forEach(Scoreboard::deactivate);
		scoreboards.clear();
	}

	private List<Entry> formatScoreboard(User user) {
		EntryBuilder builder = new EntryBuilder();
		List<String> lines;

		if (arena.getArenaState() == ArenaState.IN_GAME) {
			lines = plugin.getChatManager().getStringList("Scoreboard.Content.Playing");
		} else {
			if (arena.getArenaState() == ArenaState.ENDING) {
				lines = plugin.getChatManager().getStringList("Scoreboard.Content.Playing");
			} else {
				lines = plugin.getChatManager().getStringList("Scoreboard.Content." + arena.getArenaState().getFormattedName());
			}
		}

		for (String line : lines) {
			builder.next(formatScoreboardLine(line, user));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, User user) {
		String formattedLine = line;

		formattedLine = StringUtils.replace(formattedLine, "%time%", String.valueOf(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formattedLine = StringUtils.replace(formattedLine, "%mapname%", arena.getMapName());
		formattedLine = StringUtils.replace(formattedLine, "%players%", String.valueOf(arena.getPlayers().size()));
		formattedLine = StringUtils.replace(formattedLine, "%max_players%", String.valueOf(arena.getMaximumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%min_players%", String.valueOf(arena.getMinimumPlayers()));
		formattedLine = StringUtils.replace(formattedLine, "%coins_earned%", String.valueOf(StatsStorage.getUserStats(user.getPlayer(), StatsStorage.StatisticType.LOCAL_COINS)));
		formattedLine = StringUtils.replace(formattedLine, "%double_jumps%", String.valueOf(StatsStorage.getUserStats(user.getPlayer(), StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS)));
		formattedLine = plugin.getChatManager().colorRawMessage(formattedLine);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formattedLine = PlaceholderAPI.setPlaceholders(user.getPlayer(), formattedLine);
		}

		return formattedLine;
	}
}
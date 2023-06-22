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
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2029
 */
public class ScoreboardManager {

	private final static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
	private final static String date = formatter.format(new Date());

	private final Arena arena;
	private final Main plugin;
	private final ChatManager chatManager;
	private final Set<Scoreboard> scoreboards;

	public ScoreboardManager(final Arena arena, final Main plugin) {
		this.arena = arena;
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.scoreboards = new HashSet<>();
	}

	public void createScoreboard(final User user) {
		var scoreboard = ScoreboardLib.createScoreboard(user.getPlayer()).setHandler(new ScoreboardHandler() {

			@Override
			public String getTitle(final Player player) {
				return chatManager.message("Scoreboard.Title");
			}

			@Override
			public List<Entry> getEntries(final Player player) {
				return formatScoreboard(user);
			}
		});

		scoreboard.activate();
		scoreboards.add(scoreboard);
	}

	public void removeScoreboard(final User user) {
		final var player = user.getPlayer();

		for (final var board : scoreboards) {
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

	private List<Entry> formatScoreboard(User user) {
		final var builder = new EntryBuilder();
		final var path = "Scoreboard." + (arena.isArenaState(ArenaState.IN_GAME, ArenaState.ENDING) ? "Playing" : arena.getArenaState().getFormattedName());

		for (final var line : chatManager.getStringList(path)) {
			builder.next(formatScoreboardLine(line, user));
		}

		return builder.build();
	}

	private String formatScoreboardLine(String line, User user) {
		var formattedLine = line;

		formattedLine = formattedLine.replace("%date%", date);
		formattedLine = formattedLine.replace("%map%", arena.getMapName());
		formattedLine = formattedLine.replace("%players%", Integer.toString(arena.getPlayers().size()));
		formattedLine = formattedLine.replace("%players_left%", Integer.toString(arena.getPlayersLeft().size()));
		formattedLine = formattedLine.replace("%min_players%", Integer.toString(arena.getMinimumPlayers()));
		formattedLine = formattedLine.replace("%max_players%", Integer.toString(arena.getMaximumPlayers()));
		formattedLine = formattedLine.replace("%time%", Integer.toString(arena.getTimer()));
		formattedLine = formattedLine.replace("%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formattedLine = formattedLine.replace("%coins_earned%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_COINS)));
		formattedLine = formattedLine.replace("%double_jumps%", Integer.toString(user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS)));
		formattedLine = formattedLine.replace("%max_double_jumps%", Integer.toString(plugin.getPermissionManager().getDoubleJumps(user.getPlayer())));

		return chatManager.rawMessage(formattedLine);
	}
}
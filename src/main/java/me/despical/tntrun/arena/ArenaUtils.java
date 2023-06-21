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

package me.despical.tntrun.arena;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.User;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaUtils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final ChatManager chatManager = plugin.getChatManager();

	public static void updateNameTagsVisibility(User u) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.NAME_TAGS_HIDDEN)) return;

		for (final var user : plugin.getUserManager().getUsers()) {
			final var arena = user.getArena();

			if (arena == null) continue;

			var player = user.getPlayer();
			var scoreboard = player.getScoreboard();

			if (scoreboard == plugin.getServer().getScoreboardManager().getMainScoreboard()) {
				scoreboard = plugin.getServer().getScoreboardManager().getNewScoreboard();
			}

			var team = scoreboard.getTeam("TRHide");

			if (team == null) {
				team = scoreboard.registerNewTeam("TRHide");
			}

			team.setCanSeeFriendlyInvisibles(false);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

			if (arena.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING) || arena.getArenaState() == ArenaState.IN_GAME) {
				team.addEntry(u.getPlayer().getName());
			} else {
				team.removeEntry(u.getPlayer().getName());
			}

			player.setScoreboard(scoreboard);
		}
	}

	public static void addScore(User user, ScoreAction action) {
		String msg = chatManager.message("messages.score-actions.bonus-score");

		msg = msg.replace("%score%", (action.points > 0 ? "+" : "") + action.points);

		var actionBarMessage = msg;

		user.sendActionBar(actionBarMessage.replace("%action%", "").trim());

		msg = msg.replace("%action%", action.action);

		user.addStat(StatsStorage.StatisticType.LOCAL_COINS, action.points);
		user.sendRawMessage(msg);
	}

	public enum ScoreAction {

		SURVIVE_TIME(30, "survive");

		final int points;
		final String action;

		ScoreAction(int points, String path) {
			this.points = points;
			this.action = chatManager.message("messages.score-actions.%s".formatted(path));
		}
	}
}
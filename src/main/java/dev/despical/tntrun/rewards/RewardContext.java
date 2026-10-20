/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.rewards;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable player and round values captured before reward commands run.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
record RewardContext(Player player, Map<String, String> placeholders) {

    RewardContext {
        placeholders = Map.copyOf(placeholders);
    }

    static RewardContext from(Game game, User user) {
        Map<String, String> placeholders = new HashMap<>(24);
        placeholders.put("%player%", user.getName());
        placeholders.put("%uuid%", user.getUUID().toString());
        placeholders.put("%arena%", game.getArena().getId());
        placeholders.put("%timer%", Integer.toString(game.getTimer()));
        placeholders.put("%survival-time%", Integer.toString(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME)));
        placeholders.put("%placement%", "0");
        placeholders.put("%players%", Integer.toString(game.getUsers().size()));
        placeholders.put("%players-left%", Integer.toString(game.getPlayersLeft().size()));
        placeholders.put("%kills%", "0");
        placeholders.put("%double-jumps-used%", "0");
        placeholders.put("%double-jumps-left%", Integer.toString(user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS)));
        placeholders.put("%wins%", Integer.toString(user.getStatistic(Statistics.WIN)));
        placeholders.put("%games-played%", Integer.toString(user.getStatistic(Statistics.GAMES_PLAYED)));
        placeholders.put("%win-streak%", Integer.toString(user.getStatistic(Statistics.WIN_STREAK)));
        placeholders.put("%personal-best%", Integer.toString(user.getStatistic(Statistics.LONGEST_SURVIVE)));
        placeholders.put("%personal-arena-best%", Long.toString(user.getStatistic(Statistics.ARENA_BEST_TIMES).getOrDefault(game.getArena().getId(), 0L)));
        placeholders.put("%arena-record%", Long.toString(game.getArena().getRecordTime()));
        placeholders.put("%threshold%", "0");
        placeholders.put("%reward-amount%", "0");
        placeholders.put("%victim%", "");

        return new RewardContext(user.getPlayer(), placeholders);
    }

    RewardContext with(String placeholder, Object value) {
        Map<String, String> values = new HashMap<>(placeholders);
        values.put(placeholder, String.valueOf(value));
        return new RewardContext(player, values);
    }

    String format(String command) {
        String formatted = command;

        for (var placeholder : placeholders.entrySet()) {
            formatted = formatted.replace(placeholder.getKey(), placeholder.getValue());
        }

        return formatted;
    }
}

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

package dev.despical.tntrun.stats;

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.user.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Applies the persistent statistics produced by a completed round.
 *
 * @author Despical
 * <p>
 * Created at 09.08.2026
 */
public final class RoundStatistics {

    private RoundStatistics() {
    }

    public static void record(Arena arena, User user, boolean winner) {
        user.addStat(Statistics.GAMES_PLAYED);
        user.addStat(winner ? Statistics.WIN : Statistics.LOSE);

        if (winner) {
            user.addStat(Statistics.WIN_STREAK);
            int currentStreak = user.getStatistic(Statistics.WIN_STREAK);
            user.setStatisticIfHigher(Statistics.LONGEST_WIN_STREAK, currentStreak);
        } else {
            user.setStatistic(Statistics.WIN_STREAK, 0);
        }

        int surviveTime = user.getStatistic(Statistics.LOCAL_SURVIVE_TIME);
        user.setStatisticIfHigher(Statistics.LONGEST_SURVIVE, surviveTime);

        Map<String, Long> arenaTimes = new HashMap<>(user.getStatistic(Statistics.ARENA_BEST_TIMES));
        long previousBest = arenaTimes.getOrDefault(arena.getId(), 0L);

        if (surviveTime > previousBest) {
            arenaTimes.put(arena.getId(), (long) surviveTime);
            user.setStatistic(Statistics.ARENA_BEST_TIMES, arenaTimes);
        }
    }
}

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
import lombok.Getter;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Keeps one participant's starting records and reward claims for a single round.
 * Access is confined to the server thread by the gameplay lifecycle.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
@Getter
final class RewardProgress {

    private final User user;
    private final int personalBest;
    private final long personalArenaBest;
    private final Map<RewardType, Set<Integer>> claims;

    private int kills;
    private int doubleJumpsUsed;
    private boolean eliminated;

    RewardProgress(Game game, User user) {
        this.user = user;
        this.personalBest = user.getStatistic(Statistics.LONGEST_SURVIVE);
        this.personalArenaBest = user.getStatistic(Statistics.ARENA_BEST_TIMES).getOrDefault(game.getArena().getId(), 0L);
        this.claims = new EnumMap<>(RewardType.class);
    }

    boolean claim(RewardType type, int threshold) {
        return claims.computeIfAbsent(type, _ -> new HashSet<>()).add(threshold);
    }

    boolean eliminate() {
        if (eliminated) {
            return false;
        }

        eliminated = true;
        return true;
    }

    void recordKill() {
        kills++;
    }

    void recordDoubleJump() {
        doubleJumpsUsed++;
    }

    RewardContext context(Game game, int participants) {
        return RewardContext.from(game, user)
            .with("%players%", participants)
            .with("%kills%", kills)
            .with("%double-jumps-used%", doubleJumpsUsed);
    }
}

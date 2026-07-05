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

package dev.despical.tntrun.leaderboard;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 1.11.2024
 */
public record Leaderboard<T extends Comparable<T>>(String id, List<LeaderboardEntry<T>> sortedEntries, T fallbackValue) {

    @NotNull
    public LeaderboardEntry<T> getEntryAtPosition(int pos) {
        pos -= 1;

        if (pos < 0 || pos >= sortedEntries.size()) {
            return LeaderboardEntry.empty(fallbackValue);
        }

        return sortedEntries.get(pos);
    }
}

/*
TNT Run - Fast-paced arena survival for Minecraft.
Copyright (C) 2026  Berke Akçen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.despical.tntrun.leaderboard;

import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public record LeaderboardEntry<T extends Comparable<T>>(UUID uuid, String name, T value) {

    public static <T extends Comparable<T>> LeaderboardEntry<T> empty(T fallbackValue) {
        return new LeaderboardEntry<>(UUID.randomUUID(), "No Player", fallbackValue);
    }
}

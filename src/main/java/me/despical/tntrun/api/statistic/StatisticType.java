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

package me.despical.tntrun.api.statistic;

import me.despical.tntrun.user.User;

import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 1.11.2024
 */
public enum StatisticType {

    WINS("wins", true),
    LOSES("loses", true),
    COINS("coinsearned", true),
    GAMES_PLAYED("gamesplayed", true),
    LONGEST_SURVIVE("longestsurvive", true),
    SPECTATOR_NIGHT_VISION("spectatornightvision", true),
    SPECTATOR_SHOW_OTHERS("spectatorshowothers", true),
    SPECTATOR_SPEED("spectatorspeed", true),
    LOCAL_COINS("local_coins", false),
    LOCAL_DOUBLE_JUMPS("local_double_jumps", false),
    LOCAL_SURVIVE("local_survive", false);

    private final String name;
    private final boolean persistent;

    StatisticType(String name, boolean persistent) {
        this.name = name;
        this.persistent = persistent;
    }

    public static StatisticType match(String name) {
        return Stream.of(values()).filter(statisticType -> statisticType.name.equals(name)).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean shouldBeViewed() {
        return persistent && !name().startsWith("SPECTATOR");
    }

    public String from(User user) {
        return Integer.toString(user.getStat(this));
    }
}
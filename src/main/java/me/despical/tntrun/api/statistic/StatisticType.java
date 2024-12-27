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

    WINS("wins"),
    LOSES("loses"),
    COINS("coinsearned"),
    GAMES_PLAYED("gamesplayed"),
    LONGEST_SURVIVE("longestsurvive"),
    SPECTATOR_NIGHT_VISION("spectatornightvision"),
    SPECTATOR_SHOW_OTHERS("spectatorshowothers"),
    SPECTATOR_SPEED("spectatorspeed"),
    LOCAL_COINS,
    LOCAL_DOUBLE_JUMPS,
    LOCAL_SURVIVE;

    public static final StatisticType[] PERSISTENT_STATS = Stream.of(values()).filter(StatisticType::isPersistent).toArray(StatisticType[]::new);

    private final String name;
    private final boolean persistent;

    StatisticType() {
        this.name = null;
        this.persistent = false;
    }

    StatisticType(String name) {
        this.name = name;
        this.persistent = true;
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

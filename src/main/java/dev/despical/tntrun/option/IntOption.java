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

package dev.despical.tntrun.option;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public enum IntOption implements ConfigOption<Integer> {

    ARENA_TICK_PERIOD("arena-settings.tick-period", 5),
    LOBBY_WAITING_TIME("time-settings.waiting-time", 300),
    PRE_GAME_WAITING_TIME("time-settings.pre-game-waiting-time", 120),
    GAME_STARTING_TIME("time-settings.game-starting-time", 30),
    FULL_GAME_STARTING_TIME("time-settings.full-game-starting-time", 15),
    ENDING_TIME("time-settings.ending-time", 10);

    private final String path;
    private final int defaultValue;

    IntOption(String path, int defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Class<Integer> getType() {
        return Integer.class;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }
}

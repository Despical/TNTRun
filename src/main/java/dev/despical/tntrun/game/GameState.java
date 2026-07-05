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

package dev.despical.tntrun.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@RequiredArgsConstructor
public enum GameState {

    WAITING("waiting"),
    STARTING("starting"),
    IN_GAME("in-game"),
    ENDING("ending"),
    RESTARTING("restarting"),
    INACTIVE("inactive");

    @Getter
    private final String path;
    private static final Map<String, GameState> CACHE = new HashMap<>();

    public static GameState fromPath(@NotNull String path) {
        return CACHE.get(path);
    }

    static {
        for (GameState state : GameState.values()) {
            CACHE.put(state.getPath(), state);
        }
    }
}

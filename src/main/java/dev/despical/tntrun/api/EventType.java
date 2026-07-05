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

package dev.despical.tntrun.api;

import dev.despical.tntrun.api.event.game.GameEndEvent;
import dev.despical.tntrun.api.event.game.GameStartEvent;
import dev.despical.tntrun.api.event.game.GameStateChangeEvent;
import dev.despical.tntrun.api.event.game.GameStopEvent;
import dev.despical.tntrun.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.tntrun.api.event.player.PlayerLeaveGameEvent;
import dev.despical.tntrun.api.event.player.PlayerStatisticChangeEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;

/**
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
@Getter
@AllArgsConstructor
public enum EventType {

    GAME_START(GameStartEvent.class),
    GAME_END(GameEndEvent.class),
    GAME_STATE_CHANGE(GameStateChangeEvent.class),
    GAME_STOP(GameStopEvent.class),

    PLAYER_JOIN_ATTEMPT(PlayerJoinAttemptEvent.class),
    PLAYER_LEAVE(PlayerLeaveGameEvent.class),
    PLAYER_STAT_CHANGE(PlayerStatisticChangeEvent.class),;

    private final Class<? extends Event> eventClass;
}

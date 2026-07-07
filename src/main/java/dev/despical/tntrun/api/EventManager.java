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

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.api.event.game.GameEndEvent;
import dev.despical.tntrun.api.event.game.GameStartEvent;
import dev.despical.tntrun.api.event.game.GameStateChangeEvent;
import dev.despical.tntrun.api.event.game.GameStopEvent;
import dev.despical.tntrun.api.event.player.PlayerDoubleJumpEvent;
import dev.despical.tntrun.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.tntrun.api.event.player.PlayerLeaveGameEvent;
import dev.despical.tntrun.api.event.player.PlayerEliminateEvent;
import dev.despical.tntrun.api.event.player.PlayerStatisticChangeEvent;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.StopReason;
import dev.despical.tntrun.stats.StatisticType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class EventManager {

    private final EventProfiler profiler;

    public EventManager(TNTRun plugin) {
        this.profiler = new EventProfiler(plugin);
    }

    public <T extends Event> T call(T event) {
        long start = System.nanoTime();
        Bukkit.getPluginManager().callEvent(event);

        long duration = System.nanoTime() - start;
        profiler.record(event, duration);
        return event;
    }

    public <T extends Event> T callByType(EventType type, Supplier<T> supplier) {
        T event = supplier.get();
        Class<? extends Event> expected = EventRegistry.getEventClass(type);

        if (!expected.isInstance(event)) {
            String message = "EventType mismatch! Expected: %s but got: %s"
                .formatted(expected.getSimpleName(), event.getClass().getSimpleName());
            throw new IllegalArgumentException(message);
        }

        return call(event);
    }

    public void sendTimingsReport(CommandSender sender) {
        profiler.sendReport(sender);
    }

    public void reload() {
        profiler.reload();
    }

    public void gameStart(Game game) {
        callByType(EventType.GAME_START, () -> new GameStartEvent(game));
    }

    public void gameEnd(Game game) {
        callByType(EventType.GAME_END, () -> new GameEndEvent(game));
    }

    public GameStateChangeEvent gameStateChange(Game game, GameState oldState, GameState newState) {
        return callByType(EventType.GAME_STATE_CHANGE, () -> new GameStateChangeEvent(game, oldState, newState));
    }

    public void gameStop(Game game, StopReason reason) {
        gameStop(game, reason, List.of());
    }

    public void gameStop(Game game, StopReason reason, List<UUID> stoppedPlayers) {
        callByType(EventType.GAME_STOP, () -> new GameStopEvent(game, reason, stoppedPlayers));
    }

    public PlayerJoinAttemptEvent playerJoinAttempt(Player player, Game game) {
        return playerJoinAttempt(player, game, false);
    }

    public PlayerJoinAttemptEvent playerJoinAttempt(Player player, Game game, boolean spectatorJoin) {
        return callByType(EventType.PLAYER_JOIN_ATTEMPT, () -> new PlayerJoinAttemptEvent(player, game, spectatorJoin));
    }

    public void playerLeave(Player player, Game game, PlayerLeaveGameEvent.LeaveReason reason) {
        callByType(EventType.PLAYER_LEAVE, () -> new PlayerLeaveGameEvent(player, game, reason));
    }

    public PlayerDoubleJumpEvent playerDoubleJump(Player player, Game game, int jumpsLeft, double cooldownSeconds, Vector velocity) {
        return callByType(EventType.PLAYER_DOUBLE_JUMP,
            () -> new PlayerDoubleJumpEvent(player, game, jumpsLeft, cooldownSeconds, velocity));
    }

    public void playerEliminate(Player player, Game game, int playersLeft) {
        callByType(EventType.PLAYER_ELIMINATE, () -> new PlayerEliminateEvent(player, game, playersLeft));
    }

    public <T> PlayerStatisticChangeEvent<T> statChange(Player player, StatisticType<T> stat, T oldValue, T newValue) {
        return callByType(EventType.PLAYER_STAT_CHANGE,
            () -> new PlayerStatisticChangeEvent<>(player, stat, oldValue, newValue));
    }
}

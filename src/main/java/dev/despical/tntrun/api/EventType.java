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

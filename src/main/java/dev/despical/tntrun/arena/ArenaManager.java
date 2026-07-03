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

package dev.despical.tntrun.arena;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.event.player.PlayerJoinAttemptEvent;
import dev.despical.tntrun.api.event.player.PlayerLeaveGameEvent.LeaveReason;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameManager;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.StopReason;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ShutdownDetector;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@RequiredArgsConstructor
public class ArenaManager {

    private final Main plugin;

    public void joinAttempt(User user, Arena arena) {
        Game game = arena.getGame();

        if (game == null) {
            user.sendMessage("arena-not-available");
            return;
        }

        if (game.isPlaying(user)) {
            user.sendMessage("already-playing");
            return;
        }

        PlayerJoinAttemptEvent event = plugin.getEventManager().playerJoinAttempt(user.getPlayer(), game);
        if (event.isCancelled()) return;

        if (game.isState(GameState.IN_GAME)) {
            game.joinAsSpectator(user);
            return;
        }

        game.joinAsPlayer(user);
    }

    public void leaveAttempt(User user, LeaveReason reason) {
        Arena arena = user.getArena();

        if (arena == null) {
            user.sendMessage("not-playing");
            return;
        }

        Game game = arena.getGame();
        plugin.getEventManager().playerLeave(user.getPlayer(), game, reason);

        game.leaveUser(user);
    }

    public void quitPlayer(User user, Arena arena) {
        if (arena == null) {
            return;
        }

        Game game = arena.getGame();
        plugin.getEventManager().playerLeave(user.getPlayer(), game, LeaveReason.DISCONNECT);

        game.quitUser(user);
    }

    public void handleDisable() {
        GameManager gameManager = plugin.getGameManager();
        StopReason reason = resolveStopReason();

        plugin.getArenaRegistry().getArenas().stream()
            .map(Arena::getGame)
            .filter(Objects::nonNull)
            .forEach(game -> gameManager.stopGame(game, reason));
    }

    private StopReason resolveStopReason() {
        return ShutdownDetector.isShutdown()
            ? StopReason.SERVER_SHUTDOWN
            : StopReason.SERVER_RELOAD;
    }
}

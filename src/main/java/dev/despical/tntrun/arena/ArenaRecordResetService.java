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

package dev.despical.tntrun.arena;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Schedulers;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Despical
 * <p>
 * Created at 11.05.2026
 */
public class ArenaRecordResetService {

    private final TNTRun plugin;

    public ArenaRecordResetService(TNTRun plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> resetArenaRecords(Arena arena) {
        String arenaId = arena.getId();
        Set<User> activeUsers = plugin.getUserManager().getUsers();

        return plugin.getDatabase().resetArenaRecords(arenaId, activeUsers)
            .thenCompose(_ -> {
                CompletableFuture<Void> future = new CompletableFuture<>();

                Schedulers.runInTheNextTick(() -> {
                    for (User user : activeUsers) {
                        user.resetArenaStats(arenaId);
                    }

                    arena.setRecordHolderName("None");
                    arena.setRecordTime(-1L);

                    if (arena.isGameNonnull()) {
                        arena.getGame().getScoreboardManager().updateAllScoreboards();
                    }

                    plugin.getLeaderboardManager().refreshAllLeaderboards();

                    future.complete(null);
                });

                return future;
            });
    }
}

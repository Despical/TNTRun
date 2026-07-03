package dev.despical.tntrun.arena;

import dev.despical.tntrun.Main;
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

    private final Main plugin;

    public ArenaRecordResetService(Main plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> resetArenaRecords(Arena arena) {
        String arenaId = arena.getId();
        Set<User> activeUsers = plugin.getUserManager().getUsers();

        return plugin.getDatabase().resetArenaRecords(arenaId, activeUsers)
            .thenCompose(ignored -> {
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

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

package dev.despical.tntrun.database;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.stats.offline.OfflineStats;
import dev.despical.tntrun.user.User;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public sealed abstract class Database permits FlatFileStorage, MySQLStorage {

    protected static final TNTRun plugin = TNTRun.getInstance();

    public abstract void loadData(User user);

    public abstract OfflineStats loadOfflineData(OfflinePlayer player);

    public abstract Set<OfflineStats> getAllPlayers();

    public abstract void saveData(User user);

    public abstract void saveAllData();

    public abstract CompletableFuture<Void> resetArenaRecords(String arenaId, Collection<User> activeUsers);

    public abstract void shutdown();

    protected final Map<String, Long> sanitizeArenaBestTimes(Map<String, Long> times, String arenaId) {
        Map<String, Long> sanitized = new HashMap<>(times);
        sanitized.remove(arenaId);
        return sanitized;
    }
}

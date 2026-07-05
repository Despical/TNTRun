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

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.stats.StatisticType;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.stats.offline.OfflineStats;
import dev.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class FlatFileStorage extends Database {

    private final FileConfiguration config = ConfigUtils.getConfig(plugin, "data/stats");

    @Override
    public void loadData(User user) {
        String path = user.getUUID() + ".";

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleStat(user, path, type);
        }
    }

    private <T> void loadSingleStat(User user, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            user.loadStatistic(type, value);
            return;
        }

        user.loadStatistic(type, type.getDefaultValue());
    }

    @Override
    public void saveData(User user) {
        String path = user.getUUID() + ".";
        config.set(path + "name", user.getName());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            saveSingleStat(user, path, type);
        }
    }

    private <T> void saveSingleStat(User user, String path, StatisticType<T> type) {
        T value = user.getStatistic(type);
        Object serializedValue = type.serialize(value);

        config.set(path + "stats." + type.getKey(), serializedValue);
    }

    @Override
    @Nullable
    public OfflineStats loadOfflineData(OfflinePlayer player) {
        String path = player.getUniqueId() + ".";
        if (!config.contains(path + "name")) return null;

        String name = config.getString(path + "name");
        OfflineStats offlineStats = new OfflineStats(player.getUniqueId(), name);

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            loadSingleOfflineStat(offlineStats, path, type);
        }

        return offlineStats;
    }

    private <T> void loadSingleOfflineStat(OfflineStats offlineStats, String path, StatisticType<T> type) {
        String fullPath = path + "stats." + type.getKey();

        if (config.contains(fullPath)) {
            Object rawValue = config.get(fullPath);
            T value = type.deserialize(rawValue);

            offlineStats.setStat(type, value);
            return;
        }

        offlineStats.setStat(type, type.getDefaultValue());
    }

    @Override
    public Set<OfflineStats> getAllPlayers() {
        Set<OfflineStats> offlineStats = new HashSet<>();

        for (String uuid : config.getKeys(false)) {
            String name = config.getString(uuid + ".name");
            if (name == null) continue;

            OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
            if (player == null) continue;

            OfflineStats stats = loadOfflineData(player);
            if (stats != null) {
                offlineStats.add(stats);
            }
        }

        return offlineStats;
    }

    @Override
    public void saveAllData() {
        plugin.getUserManager().getUsers().forEach(this::saveData);
    }

    @Override
    public CompletableFuture<Void> resetArenaRecords(String arenaId, Collection<User> activeUsers) {
        for (User user : activeUsers) {
            saveActiveUserWithoutArenaRecords(user, arenaId);
        }

        for (String uuid : config.getKeys(false)) {
            resetStoredArenaRecords(uuid, arenaId);
        }

        ConfigUtils.saveConfig(plugin, config, "data/stats");
        return CompletableFuture.completedFuture(null);
    }

    private void saveActiveUserWithoutArenaRecords(User user, String arenaId) {
        String path = user.getUUID() + ".";
        config.set(path + "name", user.getName());

        for (StatisticType<?> type : Statistics.getPersistentStats()) {
            String fullPath = path + "stats." + type.getKey();

            if (type == Statistics.ARENA_BEST_TIMES) {
                Map<String, Long> sanitized = sanitizeArenaBestTimes(user.getStatistic(Statistics.ARENA_BEST_TIMES), arenaId);
                config.set(fullPath, Statistics.ARENA_BEST_TIMES.serialize(sanitized));
                continue;
            }

            saveSingleStatUnchecked(user, path, type);
        }
    }

    private void resetStoredArenaRecords(String uuid, String arenaId) {
        String path = uuid + ".stats.";

        Map<String, Long> arenaBestTimes = Statistics.ARENA_BEST_TIMES.deserialize(config.get(path + Statistics.ARENA_BEST_TIMES.getKey()));
        Map<String, Long> sanitizedArenaTimes = sanitizeArenaBestTimes(arenaBestTimes, arenaId);

        config.set(path + Statistics.ARENA_BEST_TIMES.getKey(), Statistics.ARENA_BEST_TIMES.serialize(sanitizedArenaTimes));
    }

    @SuppressWarnings("unchecked")
    private void saveSingleStatUnchecked(User user, String path, StatisticType<?> type) {
        saveSingleStat(user, path, (StatisticType<Object>) type);
    }

    @Override
    public void shutdown() {
        for (User user : plugin.getUserManager().getUsers()) {
            saveData(user);
        }

        ConfigUtils.saveConfig(plugin, config, "data/stats");
    }
}

/*
TNT Run - Fast-paced arena survival for Minecraft.
Copyright (C) 2026  Berke Akçen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.despical.tntrun.stats.offline;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.despical.tntrun.Main;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class StatsCacheManager {

    private final Main plugin;
    private final Cache<UUID, OfflineStats> offlineCache;

    public StatsCacheManager(Main plugin) {
        this.plugin = plugin;
        this.offlineCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();
    }

    public OfflineStats getStats(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        OfflineStats cached = offlineCache.getIfPresent(uuid);

        if (cached != null) {
            return cached;
        }

        OfflineStats loaded = plugin.getDatabase().loadOfflineData(player);
        if (loaded == null) {
            loaded = new OfflineStats(uuid, player.getName());
        }

        offlineCache.put(uuid, loaded);
        return loaded;
    }

    public void invalidate(UUID uuid) {
        offlineCache.invalidate(uuid);
    }
}

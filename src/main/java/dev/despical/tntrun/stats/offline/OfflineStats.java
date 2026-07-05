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

package dev.despical.tntrun.stats.offline;

import dev.despical.tntrun.stats.StatisticType;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class OfflineStats {

    @Getter
    private final UUID uuid;

    @Getter
    private final String name;

    private final Map<StatisticType<?>, Object> stats;

    public OfflineStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.stats = new HashMap<>();
    }

    public <T> void setStat(StatisticType<T> stat, T value) {
        stats.put(stat, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getStat(StatisticType<T> type) {
        return (T) stats.computeIfAbsent(type, stat -> {
            if (stat.getDefaultValue() instanceof Map) {
                return new HashMap<>();
            }

            return stat.getDefaultValue();
        });
    }
}

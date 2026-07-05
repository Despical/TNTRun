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

package dev.despical.tntrun.stats;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.despical.tntrun.Main;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class Statistics {

    private static final Gson gson = new GsonBuilder().create();

    public static final StatisticType<Integer> WIN = createIntStat("wins");
    public static final StatisticType<Integer> LOSE = createIntStat("loses");
    public static final StatisticType<Integer> WIN_STREAK = createIntStat("win_streak");
    public static final StatisticType<Integer> LONGEST_WIN_STREAK = createIntStat("longest_win_streak");
    public static final StatisticType<Integer> GAMES_PLAYED = createIntStat("games_played");
    public static final StatisticType<Integer> LONGEST_SURVIVE = createIntStat("longest_survive");
    public static final StatisticType<Integer> SPECTATOR_NIGHT_VISION_LEVEL = createIntStat("spectator_night_vision_level", 1);
    public static final StatisticType<Integer> SPECTATOR_SHOW_OTHERS = createIntStat("spectator_show_others", 1);
    public static final StatisticType<Integer> SPECTATOR_SPEED = createIntStat("spectator_speed");

    public static final StatisticType<Integer> LOCAL_DOUBLE_JUMPS = createLocalIntStat();
    public static final StatisticType<Integer> LOCAL_MAX_DOUBLE_JUMPS = createLocalIntStat();
    public static final StatisticType<Integer> LOCAL_SURVIVE_TIME = createLocalIntStat();

    public static final StatisticType<Map<String, Long>> ARENA_BEST_TIMES = new StatisticType<>("arena_best_times", new HashMap<>(), (Class<Map<String, Long>>) (Class<?>) Map.class) {

        @Override
        public Object serialize(Map<String, Long> value) {
            return gson.toJson(value);
        }

        @Override
        protected Map<String, Long> parse(String value) {
            return gson.fromJson(value, new TypeToken<Map<String, Long>>() {}.getType());
        }
    };

    private static final List<StatisticType<?>> PERSISTENT_STATS = List.of(
        WIN, LOSE, WIN_STREAK, GAMES_PLAYED, LONGEST_SURVIVE, ARENA_BEST_TIMES,
        SPECTATOR_NIGHT_VISION_LEVEL, SPECTATOR_SHOW_OTHERS, SPECTATOR_SPEED
    );

    private static final List<StatisticType<?>> ALL_STATS = List.of(
        WIN, LOSE, WIN_STREAK, GAMES_PLAYED, LONGEST_SURVIVE, ARENA_BEST_TIMES,
        SPECTATOR_NIGHT_VISION_LEVEL, SPECTATOR_SHOW_OTHERS, SPECTATOR_SPEED
    );

    private static final List<StatisticType<?>> TEMPORARY_STATS = List.of(
        LOCAL_DOUBLE_JUMPS, LOCAL_MAX_DOUBLE_JUMPS, LOCAL_SURVIVE_TIME
    );

    private static StatisticType<Integer> createIntStat(String key) {
        return createIntStat(key, 0);
    }

    private static StatisticType<Integer> createIntStat(String key, int defaultValue) {
        return new StatisticType<>(key, defaultValue, Integer.class) {

            @Override
            protected Integer parse(String value) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException _) {
                    return defaultValue;
                }
            }
        };
    }

    private static StatisticType<Integer> createLocalIntStat() {
        return new StatisticType<>(null, 0, Integer.class) { };
    }

    public static List<StatisticType<?>> getPersistentStats() {
        return PERSISTENT_STATS;
    }

    public static List<StatisticType<?>> getAllStats() {
        return ALL_STATS;
    }

    public static List<StatisticType<?>> getTemporaryStats() {
        return TEMPORARY_STATS;
    }

    public static int getDoubleJumps(Player player) {
        Main plugin = Main.getInstance();
        int defaultDoubleJumps = plugin.getConfig().getInt("double-jumps.default", 5);

        if (player == null) {
            return defaultDoubleJumps;
        }

        for (String perm : plugin.getConfig().getStringList("double-jumps.permissions")) {
            if (perm.startsWith("tntrun") && player.hasPermission(perm)) {
                return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
            }
        }

        return defaultDoubleJumps;
    }
}

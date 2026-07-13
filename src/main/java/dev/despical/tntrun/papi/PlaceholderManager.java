/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke AkÃ§en
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

package dev.despical.tntrun.papi;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameManager;
import dev.despical.tntrun.leaderboard.Leaderboard;
import dev.despical.tntrun.leaderboard.LeaderboardEntry;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * PlaceholderAPI expansion for TNT Run placeholders.
 *
 * @author Despical
 * <p>
 * Created at 28.01.2026
 */
public class PlaceholderManager extends PlaceholderExpansion {

    private static final String NO_ARENA = "none";
    private static final String NO_DATA = "--:--";

    private final TNTRun plugin;
    private final ArenaRegistry arenaRegistry;
    private final GameManager gameManager;

    public PlaceholderManager(TNTRun plugin) {
        this.plugin = plugin;
        this.arenaRegistry = plugin.getArenaRegistry();
        this.gameManager = plugin.getGameManager();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "tntrun";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "Despical";
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Nullable
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String id) {
        String normalized = id.toLowerCase(Locale.ENGLISH);

        switch (normalized) {
            case "arenas_total" -> {
                return Integer.toString(arenaRegistry.getArenas().size());
            }
            case "arenas_ready" -> {
                return Long.toString(arenaRegistry.getArenas().stream()
                    .filter(arena -> arena.getOption(ArenaKeys.READY))
                    .count());
            }
            case "active_games" -> {
                return Integer.toString(gameManager.getGames().size());
            }
            case "active_players" -> {
                return Integer.toString(gameManager.getGames().stream()
                    .mapToInt(game -> game.getUsers().size())
                    .sum());
            }
        }

        if (normalized.startsWith("arena:")) {
            return handleArenaPlaceholder(id);
        }

        if (normalized.startsWith("leaderboard:")) {
            return handleLeaderboardPlaceholder(id);
        }

        if (player == null) {
            return "";
        }

        User user = plugin.getUserManager().getUser(player);
        if (user == null) {
            return "";
        }

        if (normalized.startsWith("arena_best:")) {
            return handleArenaBestPlaceholder(user, id, false);
        }

        if (normalized.startsWith("arena_best_raw:")) {
            return handleArenaBestPlaceholder(user, id, true);
        }

        if (normalized.startsWith("stat:")) {
            return handleStatPlaceholder(user, id);
        }

        return switch (normalized) {
            case "name" -> user.getName();
            case "uuid" -> user.getUUID().toString();
            case "is_playing" -> Boolean.toString(user.isInArena());
            case "is_spectator" -> Boolean.toString(user.isSpectator());
            case "current_arena" -> getCurrentArenaId(user);
            case "current_arena_players" -> Integer.toString(getCurrentArenaPlayers(user));
            case "current_arena_players_left" -> Integer.toString(getCurrentArenaPlayersLeft(user));
            case "current_arena_max_players" -> Integer.toString(getCurrentArenaMaxPlayers(user));
            case "current_arena_state" -> getCurrentArenaState(user);
            case "current_arena_record_holder" -> getCurrentArenaRecordHolder(user);
            case "current_arena_record_time" -> formatCurrentArenaRecordTime(user);
            case "current_arena_record_time_raw" -> Long.toString(getCurrentArenaRecordTime(user));
            case "current_arena_best" -> formatCurrentArenaBest(user);
            case "current_arena_best_raw" -> Long.toString(getCurrentArenaBestRaw(user));
            case "current_survive_time" -> formatTimeValue(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
            case "current_survive_time_raw" -> Integer.toString(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
            case "double_jumps" -> Integer.toString(user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS));
            case "max_double_jumps" -> Integer.toString(user.getStatistic(Statistics.LOCAL_MAX_DOUBLE_JUMPS));
            case "double_jump_cooldown" -> formatDecimal(user.getCooldown("double_jump"));
            case "games_played" -> Integer.toString(user.getStatistic(Statistics.GAMES_PLAYED));
            case "wins" -> Integer.toString(user.getStatistic(Statistics.WIN));
            case "loses", "losses" -> Integer.toString(user.getStatistic(Statistics.LOSE));
            case "win_streak" -> Integer.toString(user.getStatistic(Statistics.WIN_STREAK));
            case "longest_win_streak" -> Integer.toString(user.getStatistic(Statistics.LONGEST_WIN_STREAK));
            case "longest_survive" -> formatTimeValue(user.getStatistic(Statistics.LONGEST_SURVIVE));
            case "longest_survive_raw" -> Integer.toString(user.getStatistic(Statistics.LONGEST_SURVIVE));
            case "win_rate" -> formatPercentage(user.getStatistic(Statistics.WIN), user.getStatistic(Statistics.GAMES_PLAYED));
            case "spectator_speed" -> Integer.toString(user.getStatistic(Statistics.SPECTATOR_SPEED));
            case "spectator_night_vision_level" -> Integer.toString(user.getStatistic(Statistics.SPECTATOR_NIGHT_VISION_LEVEL));
            case "spectator_show_others" -> Boolean.toString(user.getStatistic(Statistics.SPECTATOR_SHOW_OTHERS) == 1);
            default -> "";
        };
    }

    // %tntrun_arena:<arena>:players%
    private String handleArenaPlaceholder(String id) {
        String[] data = id.split(":");
        if (data.length < 3) {
            return "";
        }

        Arena arena = arenaRegistry.getArena(data[1]);
        if (arena == null) {
            return "";
        }

        String key = data[2].toLowerCase(Locale.ENGLISH);
        Game game = arena.getGame();

        return switch (key) {
            case "players" -> Integer.toString(game != null ? game.getUsers().size() : 0);
            case "players_left" -> Integer.toString(game != null ? game.getPlayersLeft().size() : 0);
            case "min_players", "min-players" -> Integer.toString(arena.getOption(ArenaKeys.MIN_PLAYERS));
            case "max_players", "max-players" -> Integer.toString(arena.getOption(ArenaKeys.MAX_PLAYERS));
            case "ready" -> Boolean.toString(arena.getOption(ArenaKeys.READY));
            case "map", "map_name", "map-name" -> arena.getOption(ArenaKeys.MAP_NAME);
            case "map_author", "map-author" -> arena.getOption(ArenaKeys.MAP_AUTHOR);
            case "state" -> game != null ? game.getState().getPath() : "inactive";
            case "record_holder" -> arena.getRecordHolderName();
            case "record_time" -> formatTimeValue(arena.getRecordTime());
            case "record_time_raw" -> Long.toString(arena.getRecordTime());
            case "scoreboard_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED));
            case "bossbar_enabled" -> Boolean.toString(arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED));
            case "potion_effects_count" -> Integer.toString(arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS).size());
            case "has_potion_effects" -> Boolean.toString(!arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS).isEmpty());
            default -> "";
        };
    }

    // %tntrun_leaderboard:<stat>:<position>:name%
    // %tntrun_leaderboard:wins:1:name%
    // %tntrun_leaderboard:arena_time_arena1:1:formatted_value%
    private String handleLeaderboardPlaceholder(String id) {
        String[] data = id.split(":");
        if (data.length < 4) {
            return "";
        }

        int position;
        try {
            position = Integer.parseInt(data[2]);
        } catch (NumberFormatException _) {
            return "";
        }

        String statName = data[1];
        Leaderboard<?> leaderboard = plugin.getLeaderboardManager().getLeaderboard(statName);
        if (leaderboard == null) {
            return "";
        }

        LeaderboardEntry<?> entry = leaderboard.getEntryAtPosition(position);
        String key = data[3].toLowerCase(Locale.ENGLISH);

        return switch (key) {
            case "name" -> entry.name();
            case "uuid" -> entry.uuid().toString();
            case "value" -> String.valueOf(entry.value());
            case "formatted_value" -> formatLeaderboardValue(statName, entry.value());
            default -> "";
        };
    }

    private String handleArenaBestPlaceholder(User user, String id, boolean raw) {
        String[] data = id.split(":");
        if (data.length < 2) {
            return "";
        }

        long bestTime = getArenaBestTime(user, data[1]);
        return raw ? Long.toString(bestTime) : formatTimeValue(bestTime);
    }

    private String handleStatPlaceholder(User user, String id) {
        String[] data = id.split(":");
        if (data.length < 2) {
            return "";
        }

        return switch (data[1].toLowerCase(Locale.ENGLISH)) {
            case "games_played" -> Integer.toString(user.getStatistic(Statistics.GAMES_PLAYED));
            case "wins" -> Integer.toString(user.getStatistic(Statistics.WIN));
            case "loses", "losses" -> Integer.toString(user.getStatistic(Statistics.LOSE));
            case "win_streak" -> Integer.toString(user.getStatistic(Statistics.WIN_STREAK));
            case "longest_win_streak" -> Integer.toString(user.getStatistic(Statistics.LONGEST_WIN_STREAK));
            case "longest_survive" -> Integer.toString(user.getStatistic(Statistics.LONGEST_SURVIVE));
            case "spectator_speed" -> Integer.toString(user.getStatistic(Statistics.SPECTATOR_SPEED));
            case "spectator_night_vision_level" -> Integer.toString(user.getStatistic(Statistics.SPECTATOR_NIGHT_VISION_LEVEL));
            case "spectator_show_others" -> Integer.toString(user.getStatistic(Statistics.SPECTATOR_SHOW_OTHERS));
            default -> "";
        };
    }

    private String getCurrentArenaId(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getId() : NO_ARENA;
    }

    private int getCurrentArenaPlayers(User user) {
        Arena arena = user.getArena();
        return arena != null && arena.getGame() != null ? arena.getGame().getUsers().size() : 0;
    }

    private int getCurrentArenaPlayersLeft(User user) {
        Arena arena = user.getArena();
        return arena != null && arena.getGame() != null ? arena.getGame().getPlayersLeft().size() : 0;
    }

    private int getCurrentArenaMaxPlayers(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getOption(ArenaKeys.MAX_PLAYERS) : 0;
    }

    private String getCurrentArenaState(User user) {
        Arena arena = user.getArena();
        return arena != null && arena.getGame() != null ? arena.getGame().getState().getPath() : "inactive";
    }

    private String getCurrentArenaRecordHolder(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getRecordHolderName() : NO_ARENA;
    }

    private String formatCurrentArenaRecordTime(User user) {
        return formatTimeValue(getCurrentArenaRecordTime(user));
    }

    private long getCurrentArenaRecordTime(User user) {
        Arena arena = user.getArena();
        return arena != null ? arena.getRecordTime() : -1L;
    }

    private String formatCurrentArenaBest(User user) {
        return formatTimeValue(getCurrentArenaBestRaw(user));
    }

    private long getCurrentArenaBestRaw(User user) {
        Arena arena = user.getArena();
        return arena != null ? getArenaBestTime(user, arena.getId()) : -1L;
    }

    private long getArenaBestTime(User user, String arenaId) {
        Map<String, Long> arenaBestTimes = user.getStatistic(Statistics.ARENA_BEST_TIMES);
        return arenaBestTimes.getOrDefault(arenaId, -1L);
    }

    private String formatLeaderboardValue(String statName, Object value) {
        if ((statName.equals("longest_survive") || statName.startsWith("arena_time_")) && value instanceof Number number) {
            return formatTimeValue(number.longValue());
        }

        return String.valueOf(value);
    }

    private String formatPercentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return "0.00";
        }

        return String.format(Locale.US, "%.2f", (numerator * 100.0D) / denominator);
    }

    private String formatDecimal(double value) {
        return String.format(Locale.US, "%.1f", Math.max(0D, value));
    }

    private String formatTimeValue(long seconds) {
        return seconds >= 0 ? Utils.formatTime(seconds) : NO_DATA;
    }
}

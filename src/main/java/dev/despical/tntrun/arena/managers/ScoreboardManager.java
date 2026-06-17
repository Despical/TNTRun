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

package dev.despical.tntrun.arena.managers;

import dev.despical.commons.scoreboard.Scoreboard;
import dev.despical.commons.scoreboard.ScoreboardHandler;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.scoreboard.common.Entry;
import dev.despical.commons.scoreboard.common.EntryBuilder;
import dev.despical.commons.string.StringFormatUtils;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.ArenaState;
import dev.despical.tntrun.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static dev.despical.tntrun.api.statistic.StatisticType.LOCAL_COINS;
import static dev.despical.tntrun.api.statistic.StatisticType.LOCAL_DOUBLE_JUMPS;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2029
 */
public class ScoreboardManager {

    private static final String date = StringFormatUtils.formatToday();

    private final Arena arena;
    private final Main plugin;
    private final ChatManager chatManager;
    private final String[] doubleJumpColors;
    private final Set<Scoreboard> scoreboards;

    public ScoreboardManager(Arena arena, Main plugin) {
        this.arena = arena;
        this.plugin = plugin;
        this.chatManager = plugin.getChatManager();
        this.doubleJumpColors = chatManager.message("Scoreboard.Double-Jumps").split(":");
        this.scoreboards = new HashSet<>();
    }

    public void createScoreboard(User user) {
        if (!BooleanOption.SCOREBOARD_ENABLED.value()) {
            return;
        }

        Scoreboard scoreboard = ScoreboardLib.createScoreboard(user.getPlayer()).setHandler(new ScoreboardHandler() {

            @Override
            public Component getTitle(Player player) {
                return Component.text("");
            }

            @Override
            public List<Entry> getEntries(Player player) {
                return formatScoreboard(user);
            }
        });

        scoreboard.activate();
        scoreboards.add(scoreboard);
    }

    public void removeScoreboard(User user) {
        Player player = user.getPlayer();

        for (Scoreboard board : scoreboards) {
            if (board.getHolder().equals(player)) {
                scoreboards.remove(board);
                board.deactivate();
                return;
            }
        }
    }

    public void stopAllScoreboards() {
        scoreboards.forEach(Scoreboard::deactivate);
        scoreboards.clear();
    }

    private List<Entry> formatScoreboard(User user) {
        EntryBuilder builder = new EntryBuilder();
        String path = "Scoreboard." + (arena.isArenaState(ArenaState.IN_GAME, ArenaState.ENDING) ? "Playing" : arena.getArenaState().getDefaultName());

        for (String line : chatManager.getStringList(path)) {
            builder.next(formatScoreboardLine(line, user));
        }

        return builder.build();
    }

    private String formatScoreboardLine(String line, User user) {
        int jumps = user.getStat(LOCAL_DOUBLE_JUMPS), max = plugin.getPermissionManager().getDoubleJumps(user.getPlayer());

        line = line.replace("%max_double_jumps%", Integer.toString(max));
        line = line.replace("%double_jumps%", getDoubleJumpColor(jumps, max) + jumps);
        line = line.replace("%date%", date);
        line = line.replace("%coins_earned%", Integer.toString(user.getStat(LOCAL_COINS)));
        return chatManager.formatMessage(arena, line);
    }

    public String getDoubleJumpColor(int amount, int max) {
        final int percentage = (amount * 100) / max;

        if (percentage == 0)
            return doubleJumpColors[2];
        if (percentage >= 60)
            return doubleJumpColors[0];
        return doubleJumpColors[1];
    }
}

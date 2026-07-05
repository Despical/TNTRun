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

package dev.despical.tntrun.scoreboard;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.scoreboard.Scoreboard;
import dev.despical.commons.scoreboard.ScoreboardHandler;
import dev.despical.commons.scoreboard.ScoreboardLib;
import dev.despical.commons.scoreboard.common.Entry;
import dev.despical.commons.scoreboard.common.EntryBuilder;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.scoreboard.formatter.GlobalFormatter;
import dev.despical.tntrun.scoreboard.formatter.StateFormatter;
import dev.despical.tntrun.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class ScoreboardManager {

    private static final Main plugin = Main.getInstance();
    private static final String HIDDEN_NAME_TAG_TEAM = "TRHide";
    private static final Map<GameState, List<StateFormatter>> STATE_FORMATTER_CACHE;

    static {
        Map<GameState, List<StateFormatter>> map = new EnumMap<>(GameState.class);

        for (var formatter : StateFormatter.values()) {
            map.computeIfAbsent(formatter.getState(), _ -> new ArrayList<>()).add(formatter);
        }

        STATE_FORMATTER_CACHE = Collections.unmodifiableMap(map);
    }

    private final Game game;
    private final Map<UUID, Scoreboard> scoreboards;
    private final EnumMap<GameState, ScoreboardContent> contents;
    private final EnumMap<SubState, ScoreboardContent> subContents;

    public ScoreboardManager(Game game) {
        this.game = game;
        this.scoreboards = new HashMap<>();
        this.contents = new EnumMap<>(GameState.class);
        this.subContents = new EnumMap<>(SubState.class);
        this.loadContents();
    }

    public void createScoreboard(Player player) {
        Scoreboard scoreboard = ScoreboardLib.createScoreboard(player);
        scoreboard.setHandler(new ScoreboardHandler() {

            @Override
            public Component getTitle(Player player) {
                return getScoreboardTitle();
            }

            @Override
            public List<Entry> getEntries(Player player) {
                return getLines(player);
            }
        });

        scoreboard.disableAutoUpdate();
        scoreboard.activate();
        scoreboard.update();

        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    public void removeScoreboard(Player player) {
        Scoreboard scoreboard = scoreboards.remove(player.getUniqueId());

        if (scoreboard != null) {
            scoreboard.deactivate();
        }
    }

    public void removeAllScoreboards() {
        scoreboards.values().forEach(Scoreboard::deactivate);
        scoreboards.clear();
    }

    public void updateScoreboard(Player player) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
        scoreboard.update();
    }

    public void updateAllScoreboards() {
        game.getPlayers().forEach(this::updateScoreboard);
    }

    public void updateNameTagsVisibility() {
        if (!BooleanOption.NAME_TAGS_HIDDEN.value()) {
            return;
        }

        List<String> entries = game.getUsers()
            .stream()
            .map(User::getName)
            .toList();

        game.getPlayers().forEach(player -> applyHiddenNameTagTeam(player, entries));
    }

    private void applyHiddenNameTagTeam(Player viewer, List<String> entries) {
        org.bukkit.scoreboard.Scoreboard scoreboard = getOrCreatePlayerScoreboard(viewer);
        Team team = getOrCreateHiddenNameTagTeam(scoreboard);

        entries.forEach(team::addEntry);
        viewer.setScoreboard(scoreboard);
    }

    private org.bukkit.scoreboard.Scoreboard getOrCreatePlayerScoreboard(Player player) {
        var scoreboard = player.getScoreboard();

        if (scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
            return Bukkit.getScoreboardManager().getNewScoreboard();
        }

        return scoreboard;
    }

    private Team getOrCreateHiddenNameTagTeam(org.bukkit.scoreboard.Scoreboard scoreboard) {
        Team team = scoreboard.getTeam(HIDDEN_NAME_TAG_TEAM);

        if (team == null) {
            team = scoreboard.registerNewTeam(HIDDEN_NAME_TAG_TEAM);
        }

        team.setCanSeeFriendlyInvisibles(false);
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        return team;
    }

    private Component getScoreboardTitle() {
        String title = getCurrentContent().title;
        title = title.replace("%timer%", formatIntoMMSS(game.getTimer()));
        return MiniMessage.miniMessage().deserialize(title);
    }

    private List<Entry> getLines(Player player) {
        ScoreboardContent content = getCurrentContent();
        EntryBuilder builder = new EntryBuilder();

        User user = plugin.getUserManager().getUser(player);

        for (String line : content.lines) {
            builder.next(formatLine(line, user));
        }

        return builder.build();
    }

    private String formatLine(String line, User user) {
        String result = GlobalFormatter.GLOBAL_FORMATTER.format(user, game, line);
        List<StateFormatter> formatters = STATE_FORMATTER_CACHE.get(game.getState());

        if (formatters != null) {
            for (StateFormatter formatter : formatters) {
                result = formatter.apply(user, game, result);
            }
        }

        return result;
    }

    private ScoreboardContent getCurrentContent() {
        return switch (game.getState()) {
            case STARTING -> {
                Arena arena = game.getArena();

                int current = game.getPlayers().size();
                int min = arena.getOption(ArenaKeys.MIN_PLAYERS);
                int max = arena.getOption(ArenaKeys.MAX_PLAYERS);

                if (current >= max) {
                    yield subContents.get(SubState.STARTING_FULL);
                } else if (current >= min) {
                    yield subContents.get(SubState.STARTING_HALF);
                }

                yield contents.get(GameState.STARTING);
            }
            default -> contents.get(game.getState());
        };
    }

    private String formatIntoMMSS(int time) {
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    public void loadContents() {
        contents.clear();
        subContents.clear();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "scoreboard");

        for (GameState state : GameState.values()) {
            loadContentEntry(config, state.getPath(), (title, lines) -> contents.put(state, new ScoreboardContent(title, lines)));
        }

        for (SubState state : SubState.values()) {
            loadContentEntry(config, state.getPath(), (title, lines) -> subContents.put(state, new ScoreboardContent(title, lines)));
        }
    }

    private void loadContentEntry(FileConfiguration config, String pathKey, BiConsumer<String, List<String>> consumer) {
        String path = pathKey + ".";
        String title = config.getString(path + "title");
        List<String> lines = config.getStringList(path + "lines");

        if (!lines.isEmpty()) {
            consumer.accept(title, lines);
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum SubState {

        STARTING_HALF("starting-half"),
        STARTING_FULL("starting-full");

        private final String path;
    }

    private record ScoreboardContent(String title, List<String> lines) {
    }
}

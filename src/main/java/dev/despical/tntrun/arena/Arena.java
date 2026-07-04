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

package dev.despical.tntrun.arena;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
@RequiredArgsConstructor
public class Arena {

    private static final Main plugin = Main.getInstance();

    private Game game;

    private final String id;
    private final Map<ArenaOption<?>, Object> options;
    private final Set<BlockState> destroyedBlocks = new HashSet<>();
    private final List<User> deaths = new ArrayList<>();
    private final List<User> winners = new ArrayList<>();

    Arena(String id) {
        this.id = id;
        this.options = new HashMap<>();
        this.registerDefaultOptions();
    }

    public boolean isGameNonnull() {
        return game != null;
    }

    public boolean isState(GameState gameState, GameState... states) {
        return game != null && game.isState(gameState, states);
    }

    public void start() {
        int period = plugin.getOptions().get(IntOption.ARENA_TICK_PERIOD);

        game = new Game(this, period);
        game.setGameState(GameState.WAITING);
        game.runTaskTimer(plugin, 0, period);
    }

    public <T> T getOption(ArenaOption<T> option) {
        Object value = options.computeIfAbsent(option, _ -> option.getDefaultValue());
        return option.getType().cast(value);
    }

    public void setOption(ArenaOption<?> option, Object value) {
        options.put(option, value);
    }

    public boolean isOptionPresent(ArenaOption<?> option) {
        return options.containsKey(option);
    }

    public boolean isReady() {
        return getOption(ArenaKeys.READY);
    }

    public String getRecordHolderName() {
        return getOption(ArenaKeys.RECORD_HOLDER_NAME);
    }

    public void setRecordHolderName(String recordHolderName) {
        setOption(ArenaKeys.RECORD_HOLDER_NAME, recordHolderName);
    }

    public long getRecordTime() {
        return getOption(ArenaKeys.RECORD_TIME);
    }

    public void setRecordTime(long recordTime) {
        setOption(ArenaKeys.RECORD_TIME, recordTime);
    }

    public int getTimer() {
        return game == null ? 0 : game.getTimer();
    }

    public void setTimer(int timer) {
        if (game != null) {
            game.setTimer(timer);
        }
    }

    public Set<User> getPlayers() {
        if (game == null) {
            return Set.of();
        }

        return game.getUsers().stream()
            .filter(user -> {
                Player player = user.getPlayer();
                return player != null && player.isOnline();
            })
            .collect(Collectors.toSet());
    }

    public Set<User> getPlayersLeft() {
        return getPlayers().stream().filter(Predicate.not(User::isSpectator)).collect(Collectors.toSet());
    }

    public boolean isInArena(User user) {
        return game != null && game.isPlaying(user);
    }

    public boolean isDeathPlayer(User user) {
        return deaths.contains(user);
    }

    public void addDeathPlayer(User user) {
        deaths.add(user);

        if (getPlayersLeft().size() < 4) {
            winners.add(user);
        }
    }

    public void addWinner(User user) {
        winners.add(user);
    }

    public void addDestroyedBlock(BlockState blockState) {
        destroyedBlocks.add(blockState);
    }

    public void cleanUpArena() {
        deaths.clear();
        winners.clear();

        var iterator = destroyedBlocks.iterator();
        while (iterator.hasNext()) {
            iterator.next().update(true);
            iterator.remove();
        }
    }

    public void updateSigns() {
        plugin.getSignManager().updateSigns(this);
    }

    private void registerDefaultOptions() {
        for (ArenaOption<?> setting : ArenaKeys.getAllKeys()) {
            options.put(setting, setting.getDefaultValue());
        }
    }

    @Override
    public String toString() {
        return "Arena[id=%s, game=%s]".formatted(id, String.valueOf(game));
    }
}

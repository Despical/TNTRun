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

package dev.despical.tntrun.arena;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.user.User;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaRegistry {

    private final Main plugin;
    @Getter
    private final FileConfiguration config;
    private final Map<String, Arena> arenas;

    public ArenaRegistry(Main plugin) {
        this.plugin = plugin;
        this.config = ConfigUtils.getConfig(plugin, "arenas");
        this.arenas = new HashMap<>();
        this.registerArenas();
    }

    public Arena getArena(User user) {
        return arenas.values()
            .stream()
            .filter(Arena::isGameNonnull)
            .filter(arena -> arena.getGame().isPlaying(user))
            .findFirst()
            .orElse(null);
    }

    public Arena getArena(Player player) {
        User user = plugin.getUserManager().getUser(player);
        return getArena(user);
    }

    public boolean isInArena(Player player) {
        return getArena(player) != null;
    }

    public Arena getArena(String id) {
        return findArena(id).orElse(null);
    }

    public Optional<Arena> findArena(String id) {
        return Optional.ofNullable(arenas.get(id));
    }

    public Optional<Arena> findArena(Player player) {
        return Optional.ofNullable(getArena(player));
    }

    public boolean isArenaExists(String id) {
        return arenas.containsKey(id);
    }

    public void registerNewArena(String id) {
        arenas.put(id, new Arena(id));
    }

    public void unregisterArena(Arena arena) {
        arenas.remove(arena.getId());

        plugin.getSignManager().removeArenaSigns(arena);

        config.set(arena.getId(), null);
    }

    public Set<Arena> getArenas() {
        return Set.copyOf(arenas.values());
    }

    public Set<String> getArenaNames() {
        return arenas.keySet();
    }

    public void registerArenas() {
        arenas.clear();

        for (String id : config.getKeys(false)) {
            Arena arena = new Arena(id);
            loadOptionsFor(arena, config);

            if (arena.getOption(ArenaKeys.READY)) {
                arena.start();
            }

            arenas.put(id, arena);
        }
    }

    private void loadOptionsFor(Arena arena, FileConfiguration config) {
        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            loadSingleOption(arena, config, option);
        }
    }

    private <T> void loadSingleOption(Arena arena, FileConfiguration config, ArenaOption<T> option) {
        String path = "%s.%s".formatted(arena.getId(), option.getKey());

        if (config.contains(path)) {
            Object rawValue = config.get(path);
            T value = option.deserialize(rawValue);

            arena.setOption(option, value);
        } else {
            arena.setOption(option, option.getDefaultValue());
        }
    }
}

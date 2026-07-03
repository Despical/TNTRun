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

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.sign.ArenaSign;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public class ArenaDataSaver {

    private final Main plugin;

    public ArenaDataSaver(Main plugin) {
        this.plugin = plugin;
    }

    public void saveAllArenas() {
        ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
        Set<Arena> arenas = arenaRegistry.getArenas();

        FileConfiguration config = arenaRegistry.getConfig();
        for (Arena arena : arenas) {
            saveArenaData(arena, config);
        }

        ConfigUtils.saveConfig(plugin, config, "arenas");
    }

    private void saveArenaData(Arena arena, FileConfiguration config) {
        String rootPath = arena.getId() + ".";

        for (ArenaOption<?> option : ArenaKeys.getPersistentKeys()) {
            saveSingleOption(arena, config, rootPath, option);
        }

        List<String> signLocations = plugin.getSignManager().getSigns(arena)
            .stream()
            .map(ArenaSign::sign)
            .map(Sign::getLocation)
            .map(LocationSerializer::toString)
            .toList();

        config.set(rootPath + "signs", signLocations);
        config.set(rootPath + "record-holder", arena.getRecordHolderName());
        config.set(rootPath + "record-time", arena.getRecordTime());
    }

    private <T> void saveSingleOption(Arena arena, FileConfiguration config, String rootPath, ArenaOption<T> option) {
        T value = arena.getOption(option);
        Object serializedValue = option.serialize(value);

        config.set(rootPath + option.getKey(), serializedValue);
    }
}

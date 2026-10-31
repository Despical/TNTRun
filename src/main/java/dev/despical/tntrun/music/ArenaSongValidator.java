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

package dev.despical.tntrun.music;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.arena.Arena;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clears arena songs whose files no longer exist.
 * <p>
 * Without this check an arena could keep a reference to a deleted song forever
 * and silently stay silent.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
@RequiredArgsConstructor
public class ArenaSongValidator {

    private final TNTRun plugin;

    /**
     * Resets every missing song to "no song" and logs what was reset.
     * Does nothing while the radio is unavailable, because the catalog would be empty.
     */
    public void validate() {
        Radio radio = plugin.getRadio();
        if (!radio.isAvailable()) {
            return;
        }

        SongCatalog catalog = radio.getCatalog();
        Map<String, Set<String>> missingByArena = new LinkedHashMap<>();

        for (Arena arena : plugin.getArenaRegistry().getArenas()) {
            ArenaMusicSettings settings = ArenaMusicSettings.of(arena);

            for (MusicPhase phase : MusicPhase.values()) {
                settings.getSong(phase)
                    .filter(song -> !catalog.contains(song))
                    .ifPresent(song -> {
                        missingByArena.computeIfAbsent(arena.getId(), _ -> new LinkedHashSet<>()).add(song);
                        settings.setSong(phase, null);
                    });
            }
        }

        if (missingByArena.isEmpty()) {
            return;
        }

        plugin.getLogger().warning("Missing songs detected. Those arena phases were reset to no music.");

        missingByArena.forEach((arenaId, songs) -> {
            plugin.getLogger().warning("Arena: " + arenaId);
            songs.forEach(song -> plugin.getLogger().warning(" - Missing song: " + song));
        });
    }
}

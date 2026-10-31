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

import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Copies the bundled songs into the plugin's {@code musics} folder exactly once.
 * <p>
 * A marker file remembers that the seeding happened, so songs that server
 * owners delete later are never restored against their will.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
@RequiredArgsConstructor
public class DefaultSongSeeder {

    private static final String SONGS_FOLDER = "musics";
    private static final String SONG_LIST_RESOURCE = "musics/default-songs.txt";
    private static final String MARKER_FILE = ".default-songs-seeded";

    private final JavaPlugin plugin;

    /**
     * Copies the bundled songs unless they were already seeded before.
     */
    public void seed() {
        File songsFolder = new File(plugin.getDataFolder(), SONGS_FOLDER);
        if (!songsFolder.exists() && !songsFolder.mkdirs()) {
            plugin.getLogger().warning("Failed to create the musics folder for the default songs.");
            return;
        }

        File markerFile = new File(songsFolder, MARKER_FILE);
        if (markerFile.exists()) {
            return;
        }

        int copiedSongs = 0;

        for (String resourcePath : readBundledSongPaths()) {
            if (new File(plugin.getDataFolder(), resourcePath).exists()) {
                continue;
            }

            plugin.saveResource(resourcePath, false);
            copiedSongs++;
        }

        try {
            Files.writeString(markerFile.toPath(), "seeded=" + copiedSongs, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to create the default song marker: " + exception.getMessage());
        }
    }

    private List<String> readBundledSongPaths() {
        try (InputStream inputStream = plugin.getResource(SONG_LIST_RESOURCE)) {
            if (inputStream == null) {
                return List.of();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .map(fileName -> SONGS_FOLDER + "/" + fileName)
                    .toList();
            }
        } catch (IOException exception) {
            plugin.getLogger().warning("Failed to read the bundled song list: " + exception.getMessage());
            return List.of();
        }
    }
}

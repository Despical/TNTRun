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

package dev.despical.tntrun.music.impl;

import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import dev.despical.tntrun.music.SongCatalog;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Loads every {@code .nbs} file of the {@code musics} folder into memory.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public class NbsSongLibrary implements SongCatalog {

    private static final String EXTENSION = ".nbs";

    private final JavaPlugin plugin;
    private final Map<String, Song> songs;

    /**
     * Creates the library and immediately reads all song files.
     *
     * @param plugin the owning plugin, used for the data folder and logging
     */
    public NbsSongLibrary(JavaPlugin plugin) {
        this.plugin = plugin;
        this.songs = new HashMap<>();
        this.load();
    }

    /**
     * Finds a loaded song by name.
     *
     * @param songName the song name without the file extension
     * @return the song, or an empty optional if it is unknown
     */
    public Optional<Song> find(String songName) {
        return Optional.ofNullable(songs.get(songName));
    }

    @Override
    public List<String> getSongNames() {
        return songs.keySet().stream().sorted(String.CASE_INSENSITIVE_ORDER).toList();
    }

    @Override
    public boolean contains(String songName) {
        return songs.containsKey(songName);
    }

    private void load() {
        File folder = new File(plugin.getDataFolder(), "musics");
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Failed to create the musics folder.");
            return;
        }

        long startedAt = System.nanoTime();
        List<String> failed = new ArrayList<>();
        List<String> loadedLines = new ArrayList<>();

        try (Stream<Path> files = Files.list(folder.toPath())) {
            files.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(EXTENSION))
                .sorted()
                .forEach(path -> loadSong(path, failed, loadedLines));
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to read the musics folder.", exception);
            return;
        }

        if (songs.isEmpty()) {
            plugin.getLogger().warning("No NBS song files found in the musics folder.");
        } else {
            logLoadedSongs(loadedLines, System.nanoTime() - startedAt);
        }

        if (!failed.isEmpty()) {
            plugin.getLogger().warning("Failed songs: " + String.join(", ", failed));
        }
    }

    private void logLoadedSongs(List<String> loadedLines, long elapsedNanos) {
        Duration total = songs.values().stream()
            .map(NbsSongLibrary::getDuration)
            .reduce(Duration.ZERO, Duration::plus);

        plugin.getLogger().info("Loaded %d song%s (%s of music) in %d ms from the musics folder:".formatted(
            songs.size(),
            songs.size() == 1 ? "" : "s",
            formatDuration(total),
            Duration.ofNanos(elapsedNanos).toMillis()
        ));

        loadedLines.forEach(line -> plugin.getLogger().info(" - " + line));
    }

    private static Duration getDuration(Song song) {
        float ticksPerSecond = song.getSpeed();

        if (ticksPerSecond <= 0) {
            return Duration.ZERO;
        }

        return Duration.ofMillis((long) (song.getLength() / ticksPerSecond * 1000));
    }

    private static String formatDuration(Duration duration) {
        long totalSeconds = duration.toSeconds();

        return "%d:%02d".formatted(totalSeconds / 60, totalSeconds % 60);
    }

    private void loadSong(Path path, List<String> failed, List<String> loadedLines) {
        String fileName = path.getFileName().toString();
        String songName = fileName.substring(0, fileName.length() - EXTENSION.length());

        try {
            Song song = NBSDecoder.parse(path.toFile());

            if (song == null) {
                failed.add(songName);
                return;
            }

            songs.put(songName, song);
            loadedLines.add("%s [%s]".formatted(songName, formatDuration(getDuration(song))));
        } catch (Exception exception) {
            failed.add(songName);

            plugin.getLogger().log(Level.WARNING, "Failed to load song: " + songName, exception);
        }
    }
}

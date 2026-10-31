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
import dev.despical.tntrun.music.SongPreviewer;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Previews songs of an {@link NbsSongLibrary} for editors.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
@RequiredArgsConstructor
public class NbsSongPreviewer implements SongPreviewer {

    private final NbsSongLibrary library;
    private final Map<UUID, NbsPlayback> previews = new HashMap<>();

    @Override
    public boolean preview(Player player, String songName) {
        Optional<Song> song = library.find(songName);
        if (song.isEmpty()) {
            return false;
        }

        stop(player);
        previews.put(player.getUniqueId(), NbsPlayback.start(song.get(), player));

        return true;
    }

    @Override
    public void stop(Player player) {
        NbsPlayback playback = previews.remove(player.getUniqueId());

        if (playback != null) {
            playback.stop();
        }
    }

    @Override
    public void stopAll() {
        previews.values().forEach(NbsPlayback::stop);
        previews.clear();
    }
}

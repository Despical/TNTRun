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

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import org.bukkit.entity.Player;

/**
 * A single song that loops for exactly one listener.
 * <p>
 * The class wraps the NoteBlockAPI player so the rest of the plugin never
 * has to deal with its lifecycle.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
final class NbsPlayback {

    private final Player listener;
    private final RadioSongPlayer songPlayer;

    private NbsPlayback(Song song, Player listener) {
        this.listener = listener;
        this.songPlayer = new RadioSongPlayer(song);
    }

    /**
     * Starts looping a song for a player.
     *
     * @param song     the song to loop
     * @param listener the player who hears it
     * @return the running playback
     */
    static NbsPlayback start(Song song, Player listener) {
        NbsPlayback playback = new NbsPlayback(song, listener);
        playback.songPlayer.setRepeatMode(RepeatMode.ONE);
        playback.songPlayer.addPlayer(listener);
        playback.songPlayer.playNextSong();
        playback.songPlayer.setPlaying(true);
        return playback;
    }

    /**
     * Stops the song and releases the NoteBlockAPI player.
     */
    void stop() {
        songPlayer.setPlaying(false);
        songPlayer.removePlayer(listener);
        songPlayer.destroy();
    }
}

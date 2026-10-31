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
import dev.despical.tntrun.music.MusicPhase;
import dev.despical.tntrun.music.MusicTransitionPolicy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Tracks what one player is currently listening to inside an arena.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
final class MusicSession {

    private final Player player;

    @Nullable
    private MusicPhase phase;
    @Nullable
    private String songName;
    @Nullable
    private NbsPlayback playback;

    MusicSession(Player player) {
        this.player = player;
    }

    /**
     * Moves the session to a phase and applies its song.
     * <p>
     * Re-entering the current phase changes nothing. Otherwise the running song
     * is kept when {@link MusicTransitionPolicy} allows it, and replaced
     * (or silenced when {@code song} is {@code null}) in every other case.
     *
     * @param newPhase         the phase that begins
     * @param newSongName      the configured song name, or {@code null} for silence
     * @param song             the loaded song, or {@code null} if there is none to play
     * @param continuePrevious whether an identical song keeps playing
     */
    void enterPhase(MusicPhase newPhase, @Nullable String newSongName, @Nullable Song song, boolean continuePrevious) {
        if (newPhase == phase) {
            return;
        }

        boolean keepPlaying = playback != null
            && MusicTransitionPolicy.keepsCurrentSong(songName, newSongName, continuePrevious);

        if (!keepPlaying) {
            stopPlayback();

            if (song != null) {
                songName = newSongName;
                playback = NbsPlayback.start(song, player);
            }
        }

        phase = newPhase;
    }

    /**
     * Stops whatever is playing and forgets the current phase.
     */
    void stop() {
        stopPlayback();
        phase = null;
    }

    private void stopPlayback() {
        if (playback != null) {
            playback.stop();
        }

        playback = null;
        songName = null;
    }
}

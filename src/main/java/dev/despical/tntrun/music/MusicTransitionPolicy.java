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

import org.jetbrains.annotations.Nullable;

/**
 * Decides what happens to the song that is currently playing when a new phase begins.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public final class MusicTransitionPolicy {

    private MusicTransitionPolicy() {
    }

    /**
     * Checks whether the song that is already playing should be left untouched.
     * <p>
     * The current song is only kept when the next phase uses the very same
     * song and the arena asked for it to continue. Otherwise the current song
     * is stopped and the next one (if any) starts from the beginning.
     *
     * @param currentSong      the song name playing now, or {@code null} if none
     * @param nextSong         the song name configured for the next phase, or {@code null} if none
     * @param continuePrevious whether the arena wants an identical song to keep playing
     * @return {@code true} if playback must not be interrupted
     */
    public static boolean keepsCurrentSong(@Nullable String currentSong, @Nullable String nextSong, boolean continuePrevious) {
        return continuePrevious && currentSong != null && currentSong.equals(nextSong);
    }
}

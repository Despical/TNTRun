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

import java.util.List;

/**
 * A read-only list of the songs the server is able to play.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public interface SongCatalog {

    /**
     * Gets the names of all songs, sorted alphabetically.
     *
     * @return the song names without file extensions
     */
    List<String> getSongNames();

    /**
     * Checks whether a song exists in the catalog.
     *
     * @param songName the song name to look for
     * @return {@code true} if the song can be played
     */
    boolean contains(String songName);

    /**
     * Creates a catalog that has no songs.
     *
     * @return an empty catalog
     */
    static SongCatalog empty() {
        return new SongCatalog() {

            @Override
            public List<String> getSongNames() {
                return List.of();
            }

            @Override
            public boolean contains(String songName) {
                return false;
            }
        };
    }
}

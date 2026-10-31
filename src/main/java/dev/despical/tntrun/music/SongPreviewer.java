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

import org.bukkit.entity.Player;

/**
 * Lets an editor listen to a song before assigning it to an arena.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public interface SongPreviewer {

    /**
     * Starts previewing a song for a player, replacing any running preview.
     *
     * @param player   the player who listens
     * @param songName the song to preview
     * @return {@code true} if the preview started, {@code false} if the song is unknown
     */
    boolean preview(Player player, String songName);

    /**
     * Stops the preview of a player, if there is one.
     *
     * @param player the player whose preview ends
     */
    void stop(Player player);

    /**
     * Stops every running preview.
     */
    void stopAll();

    /**
     * Creates a previewer that never plays anything.
     *
     * @return a silent previewer
     */
    static SongPreviewer none() {
        return new SongPreviewer() {

            @Override
            public boolean preview(Player player, String songName) {
                return false;
            }

            @Override
            public void stop(Player player) {
            }

            @Override
            public void stopAll() {
            }
        };
    }
}

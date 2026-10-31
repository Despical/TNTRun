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

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.game.GameState;
import org.bukkit.entity.Player;

/**
 * Plays the configured arena songs to the people inside an arena.
 * <p>
 * The game only reports who joined or left and which {@link GameState} an
 * arena entered. Choosing the song, continuing or restarting it and stopping
 * it are the radio's responsibility.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public interface Radio {

    /**
     * Checks whether this radio is able to play songs.
     *
     * @return {@code false} when the music backend is not installed
     */
    boolean isAvailable();

    /**
     * Gets the songs that can be assigned to arenas.
     *
     * @return the song catalog
     */
    SongCatalog getCatalog();

    /**
     * Gets the previewer used by the arena editor.
     *
     * @return the song previewer
     */
    SongPreviewer getPreviewer();

    /**
     * Starts the music that fits the arena's current state for a player who just joined.
     *
     * @param arena  the arena the player joined
     * @param player the player who joined, as a participant or spectator
     */
    void addPlayer(Arena arena, Player player);

    /**
     * Stops the music of a player who left the arena.
     *
     * @param arena  the arena the player left
     * @param player the player who left
     */
    void removePlayer(Arena arena, Player player);

    /**
     * Switches everyone in the arena to the music of the new state.
     *
     * @param arena the arena whose state changed
     * @param state the state the arena entered
     */
    void gameStateChanged(Arena arena, GameState state);

    /**
     * Stops the music of every player in the arena.
     *
     * @param arena the arena that stopped
     */
    void removeArena(Arena arena);

    /**
     * Stops all music and previews, used when the plugin shuts down.
     */
    void shutdown();
}

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

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.music.Radio;
import dev.despical.tntrun.music.SongCatalog;
import dev.despical.tntrun.music.SongPreviewer;
import org.bukkit.entity.Player;

/**
 * A {@link Radio} that never plays anything.
 * <p>
 * It is used when NoteBlockAPI is not installed, so the rest of the plugin can
 * call the radio without checking for the optional dependency.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public class EmptyRadio implements Radio {

    private final SongCatalog catalog = SongCatalog.empty();
    private final SongPreviewer previewer = SongPreviewer.none();

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public SongCatalog getCatalog() {
        return catalog;
    }

    @Override
    public SongPreviewer getPreviewer() {
        return previewer;
    }

    @Override
    public void addPlayer(Arena arena, Player player) {
    }

    @Override
    public void removePlayer(Arena arena, Player player) {
    }

    @Override
    public void gameStateChanged(Arena arena, GameState state) {
    }

    @Override
    public void removeArena(Arena arena) {
    }

    @Override
    public void shutdown() {
    }
}

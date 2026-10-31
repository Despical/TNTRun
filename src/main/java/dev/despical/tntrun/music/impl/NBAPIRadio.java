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
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.music.ArenaMusicSettings;
import dev.despical.tntrun.music.MusicPhase;
import dev.despical.tntrun.music.Radio;
import dev.despical.tntrun.music.SongCatalog;
import dev.despical.tntrun.music.SongPreviewer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A {@link Radio} backed by NoteBlockAPI.
 * <p>
 * Every listener owns a {@link MusicSession}, which lets each arena phase use a
 * different song and lets an identical song continue seamlessly between phases.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public class NBAPIRadio implements Radio {

    private final NbsSongLibrary library;
    private final NbsSongPreviewer previewer;
    private final Map<Arena, Map<UUID, MusicSession>> sessions;

    /**
     * Creates the radio and loads all songs.
     *
     * @param plugin the owning plugin
     */
    public NBAPIRadio(JavaPlugin plugin) {
        this.library = new NbsSongLibrary(plugin);
        this.previewer = new NbsSongPreviewer(library);
        this.sessions = new HashMap<>();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public SongCatalog getCatalog() {
        return library;
    }

    @Override
    public SongPreviewer getPreviewer() {
        return previewer;
    }

    @Override
    public void addPlayer(Arena arena, Player player) {
        if (!arena.isGameNonnull()) {
            return;
        }

        MusicPhase.fromGameState(arena.getGame().getState()).ifPresent(phase -> {
            MusicSession session = sessions
                .computeIfAbsent(arena, _ -> new HashMap<>())
                .computeIfAbsent(player.getUniqueId(), _ -> new MusicSession(player));

            apply(session, arena, phase);
        });
    }

    @Override
    public void removePlayer(Arena arena, Player player) {
        Map<UUID, MusicSession> arenaSessions = sessions.get(arena);

        if (arenaSessions != null) {
            MusicSession session = arenaSessions.remove(player.getUniqueId());

            if (session != null) {
                session.stop();
            }

            if (arenaSessions.isEmpty()) {
                sessions.remove(arena);
            }
        }

        previewer.stop(player);
    }

    @Override
    public void gameStateChanged(Arena arena, GameState state) {
        Optional<MusicPhase> phase = MusicPhase.fromGameState(state);

        if (phase.isEmpty()) {
            removeArena(arena);
            return;
        }

        Map<UUID, MusicSession> arenaSessions = sessions.get(arena);

        if (arenaSessions != null) {
            arenaSessions.values().forEach(session -> apply(session, arena, phase.get()));
        }
    }

    @Override
    public void removeArena(Arena arena) {
        Map<UUID, MusicSession> arenaSessions = sessions.remove(arena);

        if (arenaSessions != null) {
            arenaSessions.values().forEach(MusicSession::stop);
        }
    }

    @Override
    public void shutdown() {
        sessions.values().forEach(arenaSessions -> arenaSessions.values().forEach(MusicSession::stop));
        sessions.clear();
        previewer.stopAll();
    }

    private void apply(MusicSession session, Arena arena, MusicPhase phase) {
        ArenaMusicSettings settings = ArenaMusicSettings.of(arena);
        String songName = settings.getSong(phase).orElse(null);

        Song song = songName == null ? null : library.find(songName).orElse(null);
        session.enterPhase(phase, songName, song, settings.isContinuing(phase));
    }
}

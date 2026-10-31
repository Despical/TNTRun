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

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.game.GameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Describes a section of a round that can have its own song.
 * <p>
 * Each phase knows which arena options store its song and, when it follows
 * another phase, whether the song should keep playing if the same track was
 * already playing in the previous phase.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
@Getter
@RequiredArgsConstructor
public enum MusicPhase {

    /**
     * Players are waiting for the round to start, including the countdown.
     */
    LOBBY("lobby", ArenaKeys.MUSIC_LOBBY_SONG, null),

    /**
     * The round is running and the floor is disappearing.
     */
    IN_GAME("game", ArenaKeys.MUSIC_GAME_SONG, ArenaKeys.MUSIC_GAME_CONTINUE),

    /**
     * The round is over and the placements are being shown.
     */
    END("end", ArenaKeys.MUSIC_END_SONG, ArenaKeys.MUSIC_END_CONTINUE);

    private final String id;
    private final ArenaOption<String> songOption;
    @Nullable
    private final ArenaOption<Boolean> continueOption;

    /**
     * Checks whether this phase can inherit the song of the previous phase.
     *
     * @return {@code true} if the phase has a continue option, {@code false} for the first phase
     */
    public boolean hasContinueOption() {
        return continueOption != null;
    }

    /**
     * Resolves the music phase that belongs to a game state.
     *
     * @param state the current game state
     * @return the matching phase, or an empty optional when no music should play in that state
     */
    public static Optional<MusicPhase> fromGameState(GameState state) {
        return switch (state) {
            case WAITING, STARTING -> Optional.of(LOBBY);
            case IN_GAME -> Optional.of(IN_GAME);
            case ENDING -> Optional.of(END);
            case RESTARTING, INACTIVE -> Optional.empty();
        };
    }
}

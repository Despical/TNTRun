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

import dev.despical.tntrun.game.GameState;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MusicPhaseTest {

    @Test
    void mapsEveryGameStateToItsPhase() {
        assertEquals(Optional.of(MusicPhase.LOBBY), MusicPhase.fromGameState(GameState.WAITING));
        assertEquals(Optional.of(MusicPhase.LOBBY), MusicPhase.fromGameState(GameState.STARTING));
        assertEquals(Optional.of(MusicPhase.IN_GAME), MusicPhase.fromGameState(GameState.IN_GAME));
        assertEquals(Optional.of(MusicPhase.END), MusicPhase.fromGameState(GameState.ENDING));
        assertEquals(Optional.empty(), MusicPhase.fromGameState(GameState.RESTARTING));
        assertEquals(Optional.empty(), MusicPhase.fromGameState(GameState.INACTIVE));
    }

    @Test
    void onlyPhasesWithAPredecessorHaveContinueOptions() {
        assertFalse(MusicPhase.LOBBY.hasContinueOption());
        assertTrue(MusicPhase.IN_GAME.hasContinueOption());
        assertTrue(MusicPhase.END.hasContinueOption());
    }

    @Test
    void keepsSongOnlyWhenIdenticalAndContinuing() {
        assertTrue(MusicTransitionPolicy.keepsCurrentSong("Rude", "Rude", true));
        assertFalse(MusicTransitionPolicy.keepsCurrentSong("Rude", "Rude", false));
        assertFalse(MusicTransitionPolicy.keepsCurrentSong("Rude", "Bangarang", true));
        assertFalse(MusicTransitionPolicy.keepsCurrentSong("Rude", null, true));
        assertFalse(MusicTransitionPolicy.keepsCurrentSong(null, null, true));
    }
}

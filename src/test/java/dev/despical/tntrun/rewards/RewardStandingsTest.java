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

package dev.despical.tntrun.rewards;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.user.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
class RewardStandingsTest {

    @Test
    void equalTimesHaveStableStandingsAndTheActualWinnerStaysFirst() {
        Game game = mock(Game.class);
        ScoreRegistry scores = new ScoreRegistry(game);
        User winner = mock(User.class);
        UUID first = new UUID(0, 1);
        UUID second = new UUID(0, 2);
        UUID third = new UUID(0, 3);
        UUID fourth = new UUID(0, 4);
        when(winner.getUUID()).thenReturn(fourth);
        when(game.getUsers()).thenReturn(List.of(winner));
        scores.addScore(third, 60);
        scores.addScore(fourth, 60);
        scores.addScore(second, 60);
        scores.addScore(first, 60);
        scores.setWinner(winner);

        assertEquals(List.of(fourth, first, second), List.copyOf(scores.getTop3().keySet()));
        scores.addScore(second, 60);
        assertEquals(List.of(fourth, first, second), List.copyOf(scores.getTop3().keySet()));
        assertEquals(first, scores.getHighestScore().orElseThrow().uuid());
        assertSame(winner, scores.getWinner());
    }

    @Test
    void removedParticipantCannotDisplaceAnEligiblePodiumPlayerOrHoldTheRecord() {
        ScoreRegistry scores = new ScoreRegistry(mock(Game.class));
        UUID leaver = new UUID(0, 1);
        UUID survivor = new UUID(0, 2);
        scores.addScore(leaver, 100);
        scores.addScore(survivor, 60);
        scores.removePlayer(leaver);

        assertEquals(List.of(survivor), List.copyOf(scores.getTop3().keySet()));
        assertEquals(survivor, scores.getHighestScore().orElseThrow().uuid());
    }
}

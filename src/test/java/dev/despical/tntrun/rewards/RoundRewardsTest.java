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

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.stats.StatisticType;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
class RoundRewardsTest {

    private Game game;
    private Arena arena;
    private RewardManager manager;
    private RoundRewards rewards;
    private ScoreRegistry scores;
    private List<User> users;

    @BeforeEach
    void initializeRound() {
        game = mock(Game.class);
        arena = mock(Arena.class);
        manager = mock(RewardManager.class);
        users = new ArrayList<>();
        scores = new ScoreRegistry(game);
        when(game.getArena()).thenReturn(arena);
        when(game.getUsers()).thenReturn(users);
        when(game.getScores()).thenReturn(scores);
        when(game.isState(GameState.IN_GAME)).thenReturn(true);
        when(arena.getId()).thenReturn("test-arena");
        when(manager.survivalReward(anyInt())).thenReturn("30");
        rewards = new RoundRewards(game, manager);
    }

    @Test
    void startsOnlyActualRoundParticipants() {
        participant(1, 60);
        User spectator = participant(2, 0);
        when(game.isRoundParticipant(spectator)).thenReturn(false);
        rewards.start();

        verify(manager, times(1)).dispatch(eq(RewardType.GAME_START), any());
    }

    @Test
    void rewardsPodiumAndConsolationOnceWithStacking() {
        User winner = participant(1, 70);
        participant(2, 60);
        participant(3, 50);
        participant(4, 40);
        scores.setWinner(winner);
        rewards.start();
        finish();
        rewards.finish();

        verify(manager, times(4)).dispatch(eq(RewardType.GAME_END), any());
        verify(manager).dispatch(eq(RewardType.FIRST_PLACE), any());
        verify(manager).dispatch(eq(RewardType.SECOND_PLACE), any());
        verify(manager).dispatch(eq(RewardType.THIRD_PLACE), any());
        verify(manager).dispatch(eq(RewardType.PARTICIPATION), any());
        verify(manager, times(4)).dispatch(eq(RewardType.SURVIVAL_TIME), any());
    }

    @Test
    void excludesEarlyDeparturesFromPodiumAndCompletion() {
        User leaver = participant(1, 70);
        User winner = participant(2, 60);
        scores.setWinner(winner);
        rewards.start();
        rewards.quit(leaver);
        rewards.quit(leaver);
        users.remove(leaver);
        finish();

        assertFalse(scores.getTop3().containsKey(leaver.getUUID()));
        verify(manager).dispatch(eq(RewardType.GAME_QUIT), any());
        verify(manager).dispatch(eq(RewardType.GAME_END), any());
        verify(manager, never()).dispatch(eq(RewardType.SECOND_PLACE), any());
    }

    @Test
    void eliminatedPlayersWhoStayReceiveFinalRewards() {
        User victim = participant(1, 30);
        participant(2, 60);
        rewards.start();
        rewards.eliminated(victim, null);
        when(victim.isSpectator()).thenReturn(true);
        finish();

        verify(manager, times(2)).dispatch(eq(RewardType.GAME_END), any());
        verify(manager).dispatch(eq(RewardType.PLAYER_ELIMINATED), any());
    }

    @Test
    void lateSpectatorsCannotEarnLiveOrFinalRewards() {
        participant(1, 60);
        rewards.start();
        User spectator = participant(2, 100);
        when(game.isRoundParticipant(spectator)).thenReturn(false);
        scores.removePlayer(spectator.getUUID());
        rewards.tick(spectator);
        rewards.doubleJump(spectator);
        rewards.quit(spectator);
        finish();

        verify(manager).dispatch(eq(RewardType.GAME_END), any());
        verify(manager, never()).dispatch(eq(RewardType.DOUBLE_JUMP), any());
        verify(manager, never()).dispatch(eq(RewardType.GAME_QUIT), any());
    }

    @Test
    void survivalThresholdsRunOnceEvenAcrossRepeatedTicks() {
        User user = participant(1, 65);
        when(manager.thresholds(RewardType.SURVIVAL_MILESTONE)).thenReturn(Set.of(30, 60, 120));
        rewards.start();
        rewards.tick(user);
        rewards.tick(user);

        verify(manager).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), eq(30));
        verify(manager).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), eq(60));
        verify(manager, never()).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), eq(120));
    }

    @Test
    void newlyAddedThresholdDoesNotRepeatPreviouslyClaimedOnes() {
        User user = participant(1, 65);
        when(manager.thresholds(RewardType.SURVIVAL_MILESTONE)).thenReturn(Set.of(30));
        rewards.start();
        rewards.tick(user);
        when(manager.thresholds(RewardType.SURVIVAL_MILESTONE)).thenReturn(Set.of(30, 60));
        rewards.tick(user);

        verify(manager).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), eq(30));
        verify(manager).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), eq(60));
    }

    @Test
    void eliminatedPlayersCannotEarnFurtherLiveRewards() {
        User user = participant(1, 65);
        rewards.start();
        rewards.eliminated(user, null);
        rewards.tick(user);
        rewards.doubleJump(user);

        verify(manager, never()).dispatch(eq(RewardType.SURVIVAL_MILESTONE), any(), anyInt());
        verify(manager, never()).dispatch(eq(RewardType.DOUBLE_JUMP), any());
    }

    @Test
    void directKillIsCreditedOnceWithFinalKillCount() {
        User victim = participant(1, 30);
        User killer = participant(2, 60);
        rewards.start();
        rewards.eliminated(victim, killer);
        rewards.eliminated(victim, killer);

        ArgumentCaptor<RewardContext> context = ArgumentCaptor.forClass(RewardContext.class);
        verify(manager).dispatch(eq(RewardType.PLAYER_KILL), context.capture());
        assertEquals("Player2:1:Player1", context.getValue().format("%player%:%kills%:%victim%"));
        verify(manager).dispatch(eq(RewardType.PLAYER_ELIMINATED), any());
    }

    @Test
    void environmentalEliminationsDoNotAwardKills() {
        User victim = participant(1, 30);
        rewards.start();
        rewards.eliminated(victim, null);

        verify(manager, never()).dispatch(eq(RewardType.PLAYER_KILL), any());
    }

    @Test
    void spectatorAndSelfAttacksCannotEarnKills() {
        User victim = participant(1, 30);
        User attacker = participant(2, 30);
        when(attacker.isSpectator()).thenReturn(true);
        rewards.start();
        rewards.eliminated(victim, attacker);
        rewards.eliminated(attacker, attacker);

        verify(manager, never()).dispatch(eq(RewardType.PLAYER_KILL), any());
    }

    @Test
    void successfulJumpPreventsNoJumpWinEvenIfRemainingJumpsAreRestored() {
        User winner = participant(1, 60);
        scores.setWinner(winner);
        rewards.start();
        rewards.doubleJump(winner);
        when(winner.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS)).thenReturn(5);
        finish();

        ArgumentCaptor<RewardContext> context = ArgumentCaptor.forClass(RewardContext.class);
        verify(manager).dispatch(eq(RewardType.DOUBLE_JUMP), context.capture());
        assertEquals("1", context.getValue().format("%double-jumps-used%"));
        verify(manager, never()).dispatch(eq(RewardType.NO_DOUBLE_JUMP_WIN), any());
    }

    @Test
    void winnerWithoutJumpsEarnsBonus() {
        User winner = participant(1, 60);
        scores.setWinner(winner);
        rewards.start();
        finish();

        verify(manager).dispatch(eq(RewardType.NO_DOUBLE_JUMP_WIN), any());
    }

    @Test
    void recordsCompareAgainstBaselinesBeforeStatisticsWereUpdated() {
        User user = participant(1, 60);
        when(user.getStatistic(Statistics.LONGEST_SURVIVE)).thenReturn(50);
        when(user.getStatistic(Statistics.ARENA_BEST_TIMES)).thenReturn(Map.of("test-arena", 40L));
        when(arena.getRecordTime()).thenReturn(55L);
        rewards.start();
        when(user.getStatistic(Statistics.LONGEST_SURVIVE)).thenReturn(60);
        when(user.getStatistic(Statistics.ARENA_BEST_TIMES)).thenReturn(Map.of("test-arena", 60L));
        when(arena.getRecordTime()).thenReturn(60L);
        finish();

        verify(manager).dispatch(eq(RewardType.PERSONAL_BEST), any());
        verify(manager).dispatch(eq(RewardType.PERSONAL_ARENA_BEST), any());
        verify(manager).dispatch(eq(RewardType.ARENA_RECORD), any());
    }

    @Test
    void tiedRecordsDoNotEarnRecordRewards() {
        User user = participant(1, 60);
        when(user.getStatistic(Statistics.LONGEST_SURVIVE)).thenReturn(60);
        when(user.getStatistic(Statistics.ARENA_BEST_TIMES)).thenReturn(Map.of("test-arena", 60L));
        when(arena.getRecordTime()).thenReturn(60L);
        rewards.start();
        finish();

        verify(manager, never()).dispatch(eq(RewardType.PERSONAL_BEST), any());
        verify(manager, never()).dispatch(eq(RewardType.PERSONAL_ARENA_BEST), any());
        verify(manager, never()).dispatch(eq(RewardType.ARENA_RECORD), any());
    }

    @Test
    void tiedNewArenaRecordsHaveOneDeterministicRecipient() {
        participant(2, 60);
        participant(1, 60);
        rewards.start();
        finish();

        ArgumentCaptor<RewardContext> context = ArgumentCaptor.forClass(RewardContext.class);
        verify(manager).dispatch(eq(RewardType.ARENA_RECORD), context.capture());
        assertEquals("Player1", context.getValue().format("%player%"));
    }

    @Test
    void careerMilestonesUseUpdatedStatisticsAndExactThresholds() {
        User winner = participant(1, 60);
        User loser = participant(2, 30);
        scores.setWinner(winner);
        when(manager.thresholds(RewardType.WIN_STREAK)).thenReturn(Set.of(3));
        when(manager.thresholds(RewardType.TOTAL_WINS_MILESTONE)).thenReturn(Set.of(10));
        when(manager.thresholds(RewardType.GAMES_PLAYED_MILESTONE)).thenReturn(Set.of(10));
        rewards.start();
        when(winner.getStatistic(Statistics.WIN_STREAK)).thenReturn(3);
        when(winner.getStatistic(Statistics.WIN)).thenReturn(10);
        when(winner.getStatistic(Statistics.GAMES_PLAYED)).thenReturn(10);
        when(loser.getStatistic(Statistics.GAMES_PLAYED)).thenReturn(11);
        finish();

        verify(manager).dispatch(eq(RewardType.WIN_STREAK), any(), eq(3));
        verify(manager).dispatch(eq(RewardType.TOTAL_WINS_MILESTONE), any(), eq(10));
        verify(manager).dispatch(eq(RewardType.GAMES_PLAYED_MILESTONE), any(), eq(10));
    }

    @Test
    void stopDiscardsCompletionEligibilityAndRoundCanStartAgain() {
        User user = participant(1, 60);
        rewards.start();
        rewards.clear();
        rewards.tick(user);
        finish();
        verify(manager, never()).dispatch(eq(RewardType.GAME_END), any());

        when(game.isState(GameState.IN_GAME)).thenReturn(true);
        rewards.start();
        finish();
        verify(manager).dispatch(eq(RewardType.GAME_END), any());
    }

    @Test
    void finalContextsSurviveLaterTemporaryStatisticResets() {
        User user = participant(1, 65);
        rewards.start();
        finish();
        when(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME)).thenReturn(0);
        ArgumentCaptor<RewardContext> context = ArgumentCaptor.forClass(RewardContext.class);
        verify(manager).dispatch(eq(RewardType.SURVIVAL_TIME), context.capture());

        assertEquals("65:30", context.getValue().format("%survival-time%:%reward-amount%"));
    }

    @Test
    void zeroSurvivalPaymentDoesNotRunCommands() {
        participant(1, 5);
        when(manager.survivalReward(5)).thenReturn("0");
        rewards.start();
        finish();

        verify(manager, never()).dispatch(eq(RewardType.SURVIVAL_TIME), any());
    }

    private void finish() {
        when(game.isState(GameState.IN_GAME)).thenReturn(false);
        when(game.isState(GameState.ENDING)).thenReturn(true);
        rewards.finish();
    }

    private User participant(int id, int seconds) {
        Map<StatisticType<?>, Object> stats = new HashMap<>();
        stats.put(Statistics.LOCAL_SURVIVE_TIME, seconds);
        User user = mock(User.class, invocation -> {
            if (invocation.getMethod().getName().equals("getStatistic")) {
                StatisticType<?> type = invocation.getArgument(0);
                return stats.getOrDefault(type, type.getDefaultValue());
            }
            return RETURNS_DEFAULTS.answer(invocation);
        });
        when(user.getUUID()).thenReturn(new UUID(0, id));
        when(user.getName()).thenReturn("Player" + id);
        when(game.isRoundParticipant(user)).thenReturn(true);
        users.add(user);
        scores.addScore(user, seconds);
        return user;
    }
}

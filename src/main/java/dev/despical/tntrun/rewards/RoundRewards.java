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
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Coordinates reward eligibility and progress for one game's current round.
 * Spectator joins never create progress; leaving or stopping discards eligibility.
 * All hooks must run on the server thread, before temporary statistics are reset.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
public final class RoundRewards {

    private final Game game;
    private final RewardManager manager;
    private final Map<UUID, RewardProgress> participants;

    private int startingPlayers;
    private long arenaRecord;
    private boolean active;

    public RoundRewards(Game game, RewardManager manager) {
        this.game = game;
        this.manager = manager;
        this.participants = new HashMap<>();
    }

    /**
     * Captures record baselines and rewards the actual starting participants.
     * Call after the game's survival round and temporary statistics are ready.
     */
    public void start() {
        clear();
        active = true;
        arenaRecord = game.getArena().getRecordTime();

        for (User user : game.getUsers()) {
            if (game.isRoundParticipant(user)) {
                participants.put(user.getUUID(), new RewardProgress(game, user));
            }
        }

        startingPlayers = participants.size();
        participants.values().forEach(progress -> manager.dispatch(RewardType.GAME_START, context(progress)));
    }

    /**
     * Rewards every newly reached survival threshold once, including thresholds
     * crossed together. Claims survive configuration reloads for this round.
     *
     * @param user alive round participant
     */
    public void tick(User user) {
        RewardProgress progress = aliveProgress(user);
        if (progress == null) {
            return;
        }

        int seconds = user.getStatistic(Statistics.LOCAL_SURVIVE_TIME);
        RewardContext context = context(progress);

        manager.thresholds(RewardType.SURVIVAL_MILESTONE).stream().sorted().forEach(threshold -> {
            if (seconds >= threshold && progress.claim(RewardType.SURVIVAL_MILESTONE, threshold)) {
                manager.dispatch(RewardType.SURVIVAL_MILESTONE, context, threshold);
            }
        });
    }

    public void doubleJump(User user) {
        RewardProgress progress = aliveProgress(user);
        if (progress == null) {
            return;
        }

        progress.recordDoubleJump();
        manager.dispatch(RewardType.DOUBLE_JUMP, context(progress));
    }

    /**
     * Records a successful elimination before the game checks for its last survivor.
     * Only a direct, valid PvP attacker earns a kill; void falls have no attacker.
     *
     * @param victim eliminated participant
     * @param attacker direct killer, or null for environmental elimination
     */
    public void eliminated(User victim, User attacker) {
        RewardProgress progress = participants.get(victim.getUUID());
        if (!active || progress == null || !progress.eliminate()) {
            return;
        }

        manager.dispatch(RewardType.PLAYER_ELIMINATED, context(progress));

        RewardProgress killer = attacker == null || attacker.equals(victim) ? null : aliveProgress(attacker);
        if (killer != null) {
            killer.recordKill();
            manager.dispatch(RewardType.PLAYER_KILL, context(killer).with("%victim%", victim.getName()));
        }
    }

    public void quit(User user) {
        RewardProgress progress = participants.remove(user.getUUID());
        if (!active || progress == null) {
            return;
        }

        game.getScores().removePlayer(user.getUUID());
        manager.dispatch(RewardType.GAME_QUIT, context(progress));
    }

    /**
     * Rewards the final standings after persistent statistics and arena records
     * have been updated. Consuming the active flag prevents duplicate payouts.
     * Eliminated participants remain eligible when they stay until normal completion.
     */
    public void finish() {
        if (!active || !game.isState(GameState.ENDING)) {
            return;
        }

        active = false;
        List<UUID> podium = List.copyOf(game.getScores().getTop3().keySet());
        UUID recordHolder = game.getScores().getHighestScore().map(score -> score.uuid()).orElse(null);
        User winner = game.getScores().getWinner();

        for (RewardProgress progress : participants.values()) {
            User user = progress.getUser();
            if (!game.isRoundParticipant(user) || !game.getUsers().contains(user)) {
                continue;
            }

            int placement = podium.indexOf(user.getUUID()) + 1;
            RewardContext context = context(progress).with("%placement%", placement);
            manager.dispatch(RewardType.GAME_END, context);
            dispatchPlacement(placement, context);
            dispatchSurvival(user, context);
            dispatchRecords(progress, recordHolder, context);
            dispatchMilestone(progress, RewardType.GAMES_PLAYED_MILESTONE, user.getStatistic(Statistics.GAMES_PLAYED), context);

            if (user.equals(winner)) {
                dispatchMilestone(progress, RewardType.WIN_STREAK, user.getStatistic(Statistics.WIN_STREAK), context);
                dispatchMilestone(progress, RewardType.TOTAL_WINS_MILESTONE, user.getStatistic(Statistics.WIN), context);

                if (progress.getDoubleJumpsUsed() == 0) {
                    manager.dispatch(RewardType.NO_DOUBLE_JUMP_WIN, context);
                }
            }
        }
    }

    /**
     * Discards round state on normal restart, administrative stop or shutdown.
     * Already earned live-event rewards stay earned; no completion rewards are queued.
     */
    public void clear() {
        active = false;
        participants.clear();
        startingPlayers = 0;
        arenaRecord = 0;
    }

    private RewardProgress aliveProgress(User user) {
        RewardProgress progress = participants.get(user.getUUID());
        return active && game.isState(GameState.IN_GAME) && progress != null && !progress.isEliminated()
            && !user.isSpectator() && !game.getArena().isDeathPlayer(user) ? progress : null;
    }

    private RewardContext context(RewardProgress progress) {
        return progress.context(game, startingPlayers);
    }

    private void dispatchPlacement(int placement, RewardContext context) {
        RewardType type = switch (placement) {
            case 1 -> RewardType.FIRST_PLACE;
            case 2 -> RewardType.SECOND_PLACE;
            case 3 -> RewardType.THIRD_PLACE;
            default -> RewardType.PARTICIPATION;
        };

        manager.dispatch(type, context);
    }

    private void dispatchSurvival(User user, RewardContext context) {
        String amount = manager.survivalReward(user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
        if (!amount.equals("0")) {
            manager.dispatch(RewardType.SURVIVAL_TIME, context.with("%reward-amount%", amount));
        }
    }

    private void dispatchRecords(RewardProgress progress, UUID recordHolder, RewardContext context) {
        User user = progress.getUser();
        int seconds = user.getStatistic(Statistics.LOCAL_SURVIVE_TIME);

        if (seconds > progress.getPersonalBest()) {
            manager.dispatch(RewardType.PERSONAL_BEST, context);
        }

        if (seconds > progress.getPersonalArenaBest()) {
            manager.dispatch(RewardType.PERSONAL_ARENA_BEST, context);
        }

        if (user.getUUID().equals(recordHolder) && seconds > arenaRecord) {
            manager.dispatch(RewardType.ARENA_RECORD, context);
        }
    }

    private void dispatchMilestone(RewardProgress progress, RewardType type, int value, RewardContext context) {
        if (manager.thresholds(type).contains(value) && progress.claim(type, value)) {
            manager.dispatch(type, context, value);
        }
    }
}

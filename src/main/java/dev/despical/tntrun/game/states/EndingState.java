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

package dev.despical.tntrun.game.states;

import dev.despical.fileitems.SpecialItem;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 29.12.2025
 */
public class EndingState extends GameStateHandler {

    public EndingState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        game.captureSurvivalTimes();
        game.setTimer(IntOption.ENDING_TIME);

        var scores = game.getScores();
        if (scores.getWinner() == null) {
            scores.calculateWinner();
        }

        game.getPlacementMessenger().sendSummaryMessages();

        Location startLocation = getLocation(ArenaKeys.LOBBY_LOCATION);
        SpecialItem leaveItem = itemManager.getItem("leave-item");

        game.getPlayers().forEach(player -> {
            leaveItem.giveTo(player, "slot");

            player.teleport(startLocation);
        });

        this.applyStatistics();
        this.updateArenaRecord();
        plugin.getLeaderboardManager().refreshAllLeaderboards(game.getUsers());

        plugin.getEventManager().gameEnd(game);
    }

    @Override
    public void tick() {
        int timer = game.getTimer();

        if (timer > 0) {
            game.setTimer(--timer);
            return;
        }

        List<User> users = new ArrayList<>(game.getUsers());
        users.forEach(game::leaveUser);

        game.setGameState(GameState.RESTARTING);
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
    }

    private void applyStatistics() {
        ScoreRegistry scores = game.getScores();
        User winner = scores.getWinner();

        for (User user : game.getUsers()) {
            boolean isWinner = user.equals(winner);

            user.addStat(Statistics.GAMES_PLAYED);
            user.addStat(isWinner ? Statistics.WIN : Statistics.LOSE);
            user.setStatisticIfHigher(Statistics.LONGEST_SURVIVE, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));

            Map<String, Long> arenaTimes = new HashMap<>(user.getStatistic(Statistics.ARENA_BEST_TIMES));
            long surviveTime = user.getStatistic(Statistics.LOCAL_SURVIVE_TIME);
            long previousBest = arenaTimes.getOrDefault(arena.getId(), 0L);

            if (surviveTime > previousBest) {
                arenaTimes.put(arena.getId(), surviveTime);
                user.setStatistic(Statistics.ARENA_BEST_TIMES, arenaTimes);
            }
        }

        if (winner == null) {
            return;
        }

        winner.addStat(Statistics.WIN_STREAK);
        int currentStreak = winner.getStatistic(Statistics.WIN_STREAK);
        winner.setStatisticIfHigher(Statistics.LONGEST_WIN_STREAK, currentStreak);
    }

    private void updateArenaRecord() {
        var highestScore = game.getScores().getHighestScore().orElse(null);

        if (highestScore == null || highestScore.score() <= arena.getRecordTime()) {
            return;
        }

        arena.setRecordHolderName(highestScore.playerName());
        arena.setRecordTime(highestScore.score());
    }
}

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
import dev.despical.tntrun.arena.ArenaDataSaver;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.game.scores.ScoreRegistry;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

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
        game.setTimer(IntOption.ENDING_TIME);
        if (game.getScores().getWinner() == null) {
            game.getScores().calculateWinner();
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

            int score = scores.getScore(user.getUUID());
            user.setStatisticIfHigher(Statistics.LONGEST_SURVIVE, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));

            if (isWinner) {
                user.addStat(Statistics.WIN_STREAK);

                int currentStreak = user.getStatistic(Statistics.WIN_STREAK);
                user.setStatisticIfHigher(Statistics.LONGEST_WIN_STREAK, currentStreak);
            }
        }
    }

    private void updateArenaRecord() {
        ScoreRegistry.RecordScore highestScore = game.getScores().getHighestScore().orElse(null);

        if (highestScore == null || highestScore.score() <= arena.getRecordTime()) {
            return;
        }

        arena.setRecordHolderName(highestScore.playerName());
        arena.setRecordTime(highestScore.score());

        new ArenaDataSaver(plugin).saveAllArenas();
    }
}

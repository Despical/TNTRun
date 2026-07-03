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
        SpecialItem leaveItem = plugin.getItemManager().getItem("leave-item");

        game.getPlayers().forEach(player -> {
            leaveItem.giveTo(player, "slot");

            player.teleport(startLocation);
        });

        this.applyStatistics();

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
//            user.setStatisticIfHigher(Statistics.SCORE_RECORD, score);
            user.setStatisticIfHigher(Statistics.LONGEST_SURVIVE, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));

            if (isWinner) {
                user.addStat(Statistics.WIN_STREAK);

                int currentStreak = user.getStatistic(Statistics.WIN_STREAK);
                user.setStatisticIfHigher(Statistics.LONGEST_WIN_STREAK, currentStreak);
            }
        }
    }
}

package dev.despical.tntrun.game.states;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;

/**
 * @author Despical
 * <p>
 * Created at 27.12.2025
 */
public class StartingState extends GameStateHandler {

    public StartingState(Game game) {
        super(game);
    }

    @Override
    public void tick() {
        int timer = game.getTimer();

        if (timer > 0) {
            game.setTimer(--timer);
            handleLevelBarTimer(timer);

            if (timer == 0) {
                game.setGameState(GameState.IN_GAME);
            }
        }
    }

    @Override
    public void firstTick() {
        int preGameWaitingTime = IntOption.PRE_GAME_WAITING_TIME.value();

        if (game.getTimer() > preGameWaitingTime) {
            game.setTimer(preGameWaitingTime);
        }
    }

    @Override
    public void join(User user) {
        user.sendMessage("game-starting-in", Var.of("%timer%", game.getTimer()));

        WaitingState waitingState = game.getHandler(GameState.WAITING);
        waitingState.handlePlayerJoin(user);

        int timer = game.getTimer();
        int playerAmount = game.getPlayers().size();
        int maxPlayerAmount = arena.getOption(ArenaKeys.MAX_PLAYERS);
        int fullGameStartingTime = IntOption.FULL_GAME_STARTING_TIME.value();

        if (playerAmount >= maxPlayerAmount && timer > fullGameStartingTime) {
            game.setTimer(fullGameStartingTime);
        }

        int startingTime = IntOption.GAME_STARTING_TIME.value();

        if (playerAmount == (int) (maxPlayerAmount * 0.8) && timer > startingTime) {
            game.setTimer(startingTime);
        }
    }

    @Override
    public void leave(User user) {
        int playerAmount = game.getPlayers().size();
        int minPlayerAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);

        if (playerAmount < minPlayerAmount) {
            game.setGameState(GameState.WAITING);
            game.broadcastMessage("no-enough-players");
        }

        game.broadcastMessage("player-left", Var.ofPlayer(user));
    }
}

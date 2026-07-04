package dev.despical.tntrun.game.states;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 23.01.2026
 */
public class RestartingState extends GameStateHandler {

    public RestartingState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        game.getBlockRemovalManager().reset();
        arena.cleanUpArena();
        game.getUsers().clear();
        game.clearPlayerMetadata();
        game.setGameState(GameState.WAITING);
    }

    @Override
    public void tick() {
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

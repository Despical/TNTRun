package dev.despical.tntrun.game.states;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public non-sealed abstract class GameStateHandler extends GameStateBase {

    public GameStateHandler(Game game) {
        super(game);
    }

    public abstract void tick();

    public abstract void join(User user);

    public abstract void leave(User user);

    public void firstTick() {
    }
}

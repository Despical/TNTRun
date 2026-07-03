package dev.despical.tntrun.scoreboard.formatter;

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@FunctionalInterface
public interface LineFormatter {

    String format(User user, Game game, String line);
}

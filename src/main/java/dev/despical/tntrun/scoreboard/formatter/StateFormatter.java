package dev.despical.tntrun.scoreboard.formatter;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import lombok.Getter;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public enum StateFormatter {

    WAITING(GameState.WAITING,
        (_, game, line) -> {
            Arena arena = game.getArena();

            int minAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
            int playerAmount = game.getPlayers().size();
            int playersNeeded = Math.max(0, minAmount - playerAmount);

            Var[] vars = {
                Var.of("%players_needed%", playersNeeded),
                Var.of("%s%", playersNeeded <= 1 ? "" : "s"),
            };

            return Utils.format(line, vars);
        }
    ),

    STARTING(GameState.STARTING, WAITING::apply),

    IN_GAME(GameState.IN_GAME,
        (user, game, line) -> {
            int jumps = user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS);
            int max = Main.getInstance().getPermissionManager().getDoubleJumps(user.getPlayer());

            Var[] vars = {
                Var.of("%max_double_jumps%", max),
                Var.of("%double_jumps%", getDoubleJumpColor(jumps, max) + jumps),
                Var.of("%coins_earned%", user.getStatistic(Statistics.LOCAL_COIN)),
                Var.of("%players_left%", game.getPlayersLeft().size()),
            };

            return Utils.format(line, vars);
        }
    ),

    ENDING(GameState.ENDING, IN_GAME::apply)
    ;

    @Getter
    private final GameState state;
    private final LineFormatter formatter;

    StateFormatter(GameState state, LineFormatter formatter) {
        this.state = state;
        this.formatter = formatter;
    }

    public String apply(User user, Game game, String line) {
        return formatter.format(user, game, line);
    }

    public static String getDoubleJumpColor(int amount, int max) {
        if (max <= 0) {
            return "<red>";
        }

        final int percentage = (amount * 100) / max;

        if (percentage == 0)
            return "<red>";
        if (percentage >= 60)
            return "<green>";
        return "<yellow>";
    }
}

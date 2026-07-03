package dev.despical.tntrun.scoreboard.formatter;

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class GlobalFormatter {

    private static final String date = StringFormatUtils.formatToday();

    private GlobalFormatter() {
    }

    public static final LineFormatter GLOBAL_FORMATTER = (user, game, line) -> {
        int timer = game.getTimer();
        Arena arena = game.getArena();

        Var[] vars = {
            Var.ofPlayer(user),
            Var.of("%timer%", timer),
            Var.of("%formatted_timer%", "%02d:%02d".formatted(timer / 60, timer % 60)),
            Var.of("%date%", date),
            Var.of("%players%", game.getUsers().size()),
            Var.of("%min_players%", arena.getOption(ArenaKeys.MIN_PLAYERS)),
            Var.of("%max_players%", arena.getOption(ArenaKeys.MAX_PLAYERS)),
        };

        return Utils.format(line, vars);
    };
}

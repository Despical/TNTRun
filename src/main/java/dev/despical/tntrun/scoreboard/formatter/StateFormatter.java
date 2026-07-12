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

package dev.despical.tntrun.scoreboard.formatter;

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
            int max = user.getStatistic(Statistics.LOCAL_MAX_DOUBLE_JUMPS);

            Var[] vars = {
                Var.of("%max_double_jumps%", max),
                Var.of("%double_jumps%", getDoubleJumpColor(jumps, max) + jumps),
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
        String formatted = formatter.format(user, game, line);

        if (state == GameState.ENDING) {
            long millis = game.getSurvivalTimeMillis(user);
            long minutes = millis / 60_000L;
            long seconds = (millis / 1_000L) % 60L;
            long centiseconds = (millis / 10L) % 100L;

            formatted = Utils.format(formatted, Var.of("%formatted_survive_time%", "%02d:%02d.%02d".formatted(minutes, seconds, centiseconds)));
        }

        return formatted;
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

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

import dev.despical.commons.string.StringFormatUtils;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import lombok.experimental.UtilityClass;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@UtilityClass
public final class GlobalFormatter {

    private static final String date = StringFormatUtils.formatToday();

    public static final LineFormatter GLOBAL_FORMATTER = (user, game, line) -> {
        int timer = game.getTimer();
        Arena arena = game.getArena();

        Var[] vars = {
            Var.ofPlayer(user),
            Var.of("%timer%", timer),
            Var.of("%formatted_timer%", "%02d:%02d".formatted(timer / 60, timer % 60)),
            Var.of("%date%", date),
            Var.of("%players%", game.getUsers().size()),
            Var.of("%map_name%", arena.getOption(ArenaKeys.MAP_NAME)),
            Var.of("%map_author%", arena.getOption(ArenaKeys.MAP_AUTHOR)),
            Var.of("%map_difficulty%", arena.getOption(ArenaKeys.MAP_DIFFICULTY)),
            Var.of("%min_players%", arena.getOption(ArenaKeys.MIN_PLAYERS)),
            Var.of("%max_players%", arena.getOption(ArenaKeys.MAX_PLAYERS)),
        };

        return Utils.format(line, vars);
    };
}

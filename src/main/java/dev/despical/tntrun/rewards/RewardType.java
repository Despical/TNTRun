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

package dev.despical.tntrun.rewards;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Events that can execute configured reward commands.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
@Getter
@RequiredArgsConstructor
public enum RewardType {

    GAME_JOIN("game-join"),
    GAME_START("game-start"),
    GAME_QUIT("game-quit"),
    GAME_END("game-end"),
    FIRST_PLACE("first-place"),
    SECOND_PLACE("second-place"),
    THIRD_PLACE("third-place"),
    PARTICIPATION("participation"),
    SURVIVAL_TIME("survival-time"),
    SURVIVAL_MILESTONE("survival-milestone"),
    PLAYER_ELIMINATED("player-eliminated"),
    PLAYER_KILL("player-kill"),
    DOUBLE_JUMP("double-jump"),
    NO_DOUBLE_JUMP_WIN("no-double-jump-win"),
    PERSONAL_BEST("personal-best"),
    PERSONAL_ARENA_BEST("personal-arena-best"),
    ARENA_RECORD("arena-record"),
    WIN_STREAK("win-streak"),
    TOTAL_WINS_MILESTONE("total-wins-milestone"),
    GAMES_PLAYED_MILESTONE("games-played-milestone");

    private final String configurationPath;

    boolean hasThresholds() {
        return switch (this) {
            case SURVIVAL_MILESTONE, WIN_STREAK, TOTAL_WINS_MILESTONE, GAMES_PLAYED_MILESTONE -> true;
            default -> false;
        };
    }
}

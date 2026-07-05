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

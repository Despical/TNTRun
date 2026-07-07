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

package dev.despical.tntrun.command;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.arena.ArenaRegistry;
import dev.despical.tntrun.arena.ArenaManager;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.GameManager;
import dev.despical.tntrun.user.UserManager;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public sealed abstract class CommandCategory permits ArenaCommands, AdminCommands, PlayerCommands, DebugCommands, TabCompleters {

    protected static final TNTRun plugin = TNTRun.getInstance();
    protected static final ArenaRegistry arenaRegistry = plugin.getArenaRegistry();
    protected static final ArenaManager arenaManager = plugin.getArenaManager();
    protected static final GameManager gameManager = plugin.getGameManager();
    protected static final ChatManager chatManager = plugin.getChatManager();
    protected static final UserManager userManager = plugin.getUserManager();
}

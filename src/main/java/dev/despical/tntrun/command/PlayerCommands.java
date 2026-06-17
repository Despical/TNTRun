/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.annotations.Command;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class PlayerCommands extends CommandCategory {

    @Command(
        name = "tntrun.join",
        aliases = "tr.join",
        usage = "/%label% join <arena>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void joinCommand(User user, CommandArguments arguments) {
        String arenaId = arguments.getArgument(0);
        Arena arena = arenaRegistry.getArena(arenaId);

        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        arenaManager.joinAttempt(user, arena);
    }

    @Command(
        name = "tntrun.leave",
        aliases = "tr.leave",
        usage = "/%label% leave",
        senderType = Command.SenderType.PLAYER
    )
    public void leaveCommand(User user) {
        arenaManager.leaveAttempt(user, Reason.LEAVE_COMMAND);
    }

    @Command(
        name = "tntrun.stats",
        aliases = "tr.stats",
        usage = "/%label% stats [player]",
        senderType = Command.SenderType.PLAYER
    )
    public void statsCommand(User user, CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            new StatsMenu(user).open();
            return;
        }

        Player target = arguments.getPlayer(0).orElse(null);
        if (target != null) {
            new StatsMenu(user, target).open();
            return;
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(arguments.getFirst());

        if (offlinePlayer == null) {
            chatManager.sendMessage(arguments, "no-player-with-that-name");
            return;
        }

        new StatsMenu(user, offlinePlayer).open();
    }
}

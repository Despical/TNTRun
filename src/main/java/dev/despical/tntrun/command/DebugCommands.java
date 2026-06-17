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
import dev.despical.commandframework.annotations.Option;
import dev.despical.commandframework.debug.Debug;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Objects;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@Debug
public final class DebugCommands extends CommandCategory {

    @Option("players")
    @Command(
        name = "tntrun.debug.join",
        aliases = "tr.debug.join",
        permission = "tntrun.debug.join",
        usage = "/%label% debug join <arena> [--players=p1,p2,...]",
        min = 1,
        max = 3,
        senderType = Command.SenderType.PLAYER
    )
    public void debugJoinCommand(User user, CommandArguments arguments) {
        Arena arena = arenaRegistry.getArena(arguments.getFirst());

        if (arena == null) {
            arguments.sendMessage("&cArena ''{0}'' does not exist.", arguments.getFirst());
            return;
        }

        arenaManager.joinAttempt(user, arena);

        List<String> players = arguments.getOption("players");
        if (players != null) {
            players.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .map(userManager::getUser)
                .forEach(player -> arenaManager.joinAttempt(player, arena));
        }
    }

    @Command(
        name = "tntrun.debug.component",
        aliases = "tr.debug.component",
        permission = "tntrun.debug.component",
        usage = "/%label% debug component <message>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void debugComponentCommand(CommandArguments arguments) {
        Component component = chatManager.parseMessage(arguments.concatArguments());
        arguments.sendMessage(component);
    }

    @Command(
        name = "tntrun.debug.dump",
        aliases = "tr.debug.dump",
        permission = "tntrun.debug.dump",
        usage = "/%label% debug dump"
    )
    public void debugDumpTimingsCommand(CommandArguments arguments) {
        plugin.getEventManager().sendTimingsReport(arguments.getSender());
    }
}

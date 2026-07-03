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
import dev.despical.tntrun.api.event.player.PlayerLeaveGameEvent.LeaveReason;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.command.arguments.Arguments;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.StopReason;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class AdminCommands extends CommandCategory {

    @Command(
        name = "tntrun",
        aliases = "tr",
        fallbackPrefix = "thetntrun",
        permission = "tntrun.command.help",
        usage = "/tntrun help",
        desc = "Main command of the TNT Run."
    )
    public void mainCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage("<#00aaaa>This server is running <#55ffff>TNT Run v{0} <#00aaaa>by <#55ffff>Despical<#00aaaa>.", plugin.getDescription().getVersion());

            if (arguments.hasPermission("tntrun.admin.help")) {
                arguments.sendMessage("<#00aaaa>Commands: <#55ffff>/{0} help", arguments.getLabel());
            }

            return;
        }

        chatManager.sendMessage(arguments, "unrecognized-arguments", Var.of("%label%", arguments.getLabel()), Var.of("%arguments%", arguments.concatArguments()));
    }

    @Command(
        name = "tntrun.reload",
        aliases = "tr.reload",
        permission = "tntrun.admin.reload",
        usage = "/%label% reload",
        desc = "Reloads configuration files."
    )
    public void reloadCommand(CommandArguments arguments) {
        chatManager.loadFile();
        plugin.getOptions().reloadOptions();
        plugin.getPlayingCommandPolicy().reload();
        plugin.registerItems();
        plugin.getSoundManager().reload();
        plugin.getEventManager().reload();
        plugin.getSignManager().reload();
        gameManager.reload();

        chatManager.sendMessage(arguments, "reloaded-configuration");
    }

    @Command(
        name = "tntrun.stop",
        aliases = "tr.stop",
        permission = "tntrun.admin.stop",
        usage = "/%label% stop [arena]",
        desc = "Stops the current or specified arena game.",
        max = 1
    )
    public void stopCommand(CommandArguments arguments) {
        boolean isConsoleSender = arguments.isSenderConsole();

        if (arguments.isArgumentsEmpty()) {
            if (isConsoleSender) {
                chatManager.sendMessage(arguments, "stop-command.correct-usage", Var.of("%label%", arguments.getLabel()));
                return;
            }

            Player player = arguments.getSender();
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                chatManager.sendMessage(player, "not-playing");
                return;
            }

            gameManager.stopGame(arena.getGame(), StopReason.STOP_COMMAND);
            return;
        }

        Arena arena = arenaRegistry.getArena(arguments.getFirst());

        if (arena == null) {
            chatManager.sendMessage(arguments, "no-arena-found-with-that-name");
            return;
        }

        Game game = arena.getGame();

        if (game == null) {
            chatManager.sendMessage(arguments, "stop-command.not-playing");
            return;
        }

        gameManager.stopGame(game, StopReason.STOP_COMMAND);

        if (!isConsoleSender && game.isPlaying(arguments.<Player>getSender())) {
            return;
        }

        chatManager.sendMessage(arguments, "stop-command.stopped");
    }

    @Command(
        name = "tntrun.start",
        aliases = "tr.start",
        permission = "tntrun.admin.start",
        usage = "/%label% start [arena]",
        desc = "Starts the current or specified arena game.",
        max = 1
    )
    public void startCommand(Arguments arguments) {
        boolean isConsoleSender = arguments.isSenderConsole();

        if (arguments.isArgumentsEmpty()) {
            if (isConsoleSender) {
                arguments.sendMessage("start-command.correct-usage", Var.of("%label%", arguments.getLabel()));
                return;
            }

            Player player = arguments.getSender();
            Arena arena = arenaRegistry.getArena(player);

            if (arena == null) {
                arguments.sendMessage("start-command.you-are-not-playing");
                return;
            }

            Game game = arena.getGame();
            if (game.getUsers().isEmpty()) {
                arguments.sendMessage("start-command.no-players");
                return;
            }

            gameManager.startGame(arena.getGame());
            return;
        }

        Arena arena = arenaRegistry.getArena(arguments.getFirst());
        if (arena == null) {
            arguments.sendMessage("no-arena-found-with-that-name");
            return;
        }

        Game game = arena.getGame();
        if (game == null) {
            arguments.sendMessage("game-instance-not-present");
            return;
        }

        if (game.getUsers().isEmpty()) {
            arguments.sendMessage("start-command.no-players");
            return;
        }

        gameManager.startGame(arena.getGame());

        if (!isConsoleSender && game.isPlaying(arguments.<Player>getSender())) {
            return;
        }

        arguments.sendMessage("start-command.started");
    }

    @Command(
        name = "tntrun.help",
        aliases = "tr.help",
        permission = "tntrun.command.help",
        usage = "/%label% help"
    )
    public void helpCommand(CommandArguments arguments) {
        Var var = Var.of("%label%", arguments.getLabel());
        chatManager.sendMessage(arguments, "help-message", var);

        if (arguments.hasPermission("tntrun.admin.help")) {
            arguments.sendMessage("");
            chatManager.sendMessage(arguments, "admin-help-message", var);
            arguments.sendMessage("");

            if (BooleanOption.DEBUG.value()) {
                chatManager.sendMessage(arguments, "debug-help-message", var);
            }
        }
    }

    @Command(
        name = "tntrun.kick",
        aliases = "tr.kick",
        permission = "tntrun.admin.kick",
        usage = "/%label% kick <player>",
        desc = "Removes a player from game and teleports them to the arena's end location.",
        min = 1,
        max = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void kickCommand(CommandArguments arguments) {
        Player targetPlayer = arguments.getPlayer(0)
            .orElseGet(() -> {
                chatManager.sendMessage(arguments, "no-player-with-that-name");
                return null;
            });

        if (targetPlayer == null) {
            return;
        }

        User targetUser = plugin.getUserManager().getUser(targetPlayer);
        Arena playerArena = targetUser.getArena();

        if (playerArena == null) {
            chatManager.sendMessage(arguments, "kick-command.not-playing",
                Var.of("%player%", targetPlayer.getName()));
            return;
        }

        arenaManager.leaveAttempt(targetUser, LeaveReason.LEAVE_COMMAND);

        Location endLocation = playerArena.getOption(ArenaKeys.END_LOCATION);

        if (endLocation != null) {
            targetPlayer.teleport(endLocation);
        }

        chatManager.sendMessage(arguments, "kick-command.kicked",
            Var.of("%player%", targetPlayer.getName()),
            Var.of("%arena%", playerArena.getId()));
    }
}

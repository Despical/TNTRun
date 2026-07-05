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

import dev.despical.commandframework.annotations.Command;
import dev.despical.commandframework.annotations.Flag;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.command.arguments.Arguments;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.StopReason;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class ArenaCommands extends CommandCategory {

    @Command(
        name = "tntrun.create",
        aliases = "tr.create",
        permission = "tntrun.arena.create",
        usage = "/%label% create <arena id>",
        min = 1,
        max = 1
    )
    public void createArenaCommand(Arguments arguments) {
        String arenaId = arguments.getFirst();
        Var var = Var.of("%id%", arenaId);

        if (arenaRegistry.isArenaExists(arenaId)) {
            arguments.sendCenteredMessage("arena-already-exists", var);
            arguments.playSound(Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        arenaRegistry.registerNewArena(arenaId);

        arguments.sendCenteredMessage("created-arena", var);
        arguments.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 2.0f);
    }

    @Flag({"confirm", "cancel"})
    @Command(
        name = "tntrun.delete",
        aliases = "tr.delete",
        permission = "tntrun.arena.delete",
        usage = "/%label% delete <arena id> [--confirm] [--cancel]",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void deleteArenaCommand(Arena arena, Arguments arguments) {
        Var var = Var.of("%id%", arguments.getFirst());

        if (arena == null) {
            arguments.sendCenteredMessage("no-arena-found", var);
            arguments.playSound(Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("cancel")) {
            arguments.sendCenteredMessage("delete-cancelled", var);
            arguments.playSound(Sound.UI_BUTTON_CLICK, 1f, 1f);
            return;
        }

        if (arguments.isFlagPresent("confirm")) {
            Game game = arena.getGame();
            gameManager.stopGame(game, StopReason.ARENA_DELETED);

            arenaRegistry.unregisterArena(arena);

            arguments.sendCenteredMessage("deleted-arena", var);
            arguments.playSound(Sound.ENTITY_ITEM_BREAK, 1f, 0.5f);
            return;
        }

        arguments.sendCenteredMessage("delete-confirmation", var);
        arguments.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    @Command(
        name = "tntrun.list",
        aliases = "tr.list",
        permission = "tntrun.arena.list",
        usage = "/%label% list",
        senderType = Command.SenderType.PLAYER
    )
    public void listArenaCommand(User user, Arguments arguments) {
        Set<Arena> arenas = arenaRegistry.getArenas();

        if (arenas.isEmpty()) {
            arguments.sendMessage("no-arenas-registered");
            arguments.playSound(Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        StringJoiner arenasJoiner = new StringJoiner("<dark_gray>, ");

        for (Arena arena : arenas) {
            boolean isReady = arena.getOption(ArenaKeys.READY);

            if (isReady) {
                arenasJoiner.add(
                    "<#00E676><hover:show_text:'<#00E676><b>✔ Ready to play!</b><br><gray>Click to join.'><click:run_command:'/tr join %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            } else {
                arenasJoiner.add(
                    "<#FF5252><hover:show_text:'<#FF5252><b>✖ Setup Incomplete!</b><br><gray>Click to edit.'><click:run_command:'/tr edit %s'>%1$s</click></hover>".formatted(arena.getId())
                );
            }
        }

        arguments.sendMessage("created-arenas", Var.of("%arenas%", arenasJoiner.toString()));
        arguments.playSound(Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    @Command(
        name = "tntrun.edit",
        aliases = "tr.edit",
        permission = "tntrun.arena.edit",
        usage = "/%label% edit <arena id>",
        min = 1,
        senderType = Command.SenderType.PLAYER
    )
    public void editArenaCommand(Arena arena, Arguments arguments) {
        if (arena == null) {
            arguments.sendMessage("no-arena-found-with-that-name");
            return;
        }

        User user = userManager.getUser(arguments.<Player>getSender());
        new SetupMenu(user, arena).open();
    }
}

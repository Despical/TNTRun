/*
TNT Run - Fast-paced arena survival for Minecraft.
Copyright (C) 2026  Berke Akçen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.despical.tntrun.command;

import dev.despical.commandframework.CommandArguments;
import dev.despical.commandframework.CompleterHelper;
import dev.despical.commandframework.annotations.Completer;
import dev.despical.commandframework.debug.Debug;
import dev.despical.tntrun.option.BooleanOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class TabCompleters extends CommandCategory {

    private static final List<String> DEBUG_TIMER_VALUES = List.of("0", "30", "60", "120", "300");

    @Completer(
        name = "tntrun",
        aliases = "tr",
        permission = "tntrun.command.tabcompleter"
    )
    public List<String> onTabCompletion(CommandArguments arguments, CompleterHelper helper) {
        int length = arguments.getLength();
        List<String> availableCommands = collectAvailableCommands(arguments);

        return switch (length) {
            case 0 -> availableCommands;
            case 1 -> helper.copyMatches(0, availableCommands);
            case 2 -> {
                if (helper.equalsAny(0, "edit", "delete", "join", "stop")) {
                    yield helper.copyMatches(1, arenaRegistry.getArenaNames());
                }

                if (helper.equalsAny(0, "start") && arguments.hasPermission("tntrun.command.start")) {
                    yield helper.copyMatches(1, arenaRegistry.getArenaNames());
                }

                if (helper.equalsAny(0, "stats")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                if (helper.equalsAny(0, "kick") && arguments.hasPermission("tntrun.admin.kick")) {
                    yield helper.copyMatches(1, helper.playerNames());
                }

                yield helper.empty();
            }
            default -> helper.empty();
        };
    }

    @Debug
    @Completer(
        name = "tntrun.debug",
        aliases = "tr.debug",
        permission = "tntrun.debug.tabcompleter"
    )
    public List<String> debugTabCompleter(CommandArguments arguments, CompleterHelper helper) {
        if (arguments.isSenderConsole()) {
            return helper.empty();
        }

        if (arguments.getLength() == 1) {
            return helper.copyMatches(0, List.of("component", "dump", "join", "timer"));
        }

        if (helper.equalsAny(0, "join") && arguments.getLength() == 2) {
            return helper.copyMatches(1, arenaRegistry.getArenaNames());
        }

        if (helper.equalsAny(0, "timer")) {
            return completeDebugTimer(arguments, helper);
        }

        return helper.empty();
    }

    private List<String> completeDebugTimer(CommandArguments arguments, CompleterHelper helper) {
        if (arguments.getLength() == 2) {
            List<String> values = new ArrayList<>(arenaRegistry.getArenaNames());
            values.addAll(DEBUG_TIMER_VALUES);
            return helper.copyMatches(1, values);
        }

        if (arguments.getLength() == 3) {
            return helper.copyMatches(2, DEBUG_TIMER_VALUES);
        }

        return helper.empty();
    }

    private List<String> collectAvailableCommands(CommandArguments arguments) {
        List<String> availableCommands = new ArrayList<>(List.of("join", "leave", "stats"));

        if (arguments.hasPermission("tntrun.command.help")) {
            availableCommands.add("help");
        }

        if (arguments.hasPermission("tntrun.command.start")) {
            availableCommands.add("start");
        }

        if (arguments.hasPermission("tntrun.arena.create")) {
            availableCommands.add("create");
        }

        if (arguments.hasPermission("tntrun.arena.list")) {
            availableCommands.add("list");
        }

        if (arguments.hasPermission("tntrun.arena.edit")) {
            availableCommands.add("edit");
        }

        if (arguments.hasPermission("tntrun.arena.delete")) {
            availableCommands.add("delete");
        }

        if (arguments.hasPermission("tntrun.admin.stop")) {
            availableCommands.add("stop");
        }

        if (arguments.hasPermission("tntrun.admin.reload")) {
            availableCommands.add("reload");
        }

        if (arguments.hasPermission("tntrun.admin.kick")) {
            availableCommands.add("kick");
        }

        if (BooleanOption.DEBUG.value() && arguments.hasPermission("tntrun.debug.tabcompleter")) {
            availableCommands.add("debug");
        }

        return availableCommands;
    }
}

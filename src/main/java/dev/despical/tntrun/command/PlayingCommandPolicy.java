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

import dev.despical.tntrun.Main;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class PlayingCommandPolicy {

    public static final String BYPASS_PERMISSION = "tntrun.commandblock.bypass";

    private static final String ALLOWED_COMMANDS_PATH = "command-settings.allowed-commands";

    private final Main plugin;

    private Set<String> allowedCommands;
    private Set<String> allowedRootCommands;

    public PlayingCommandPolicy(Main plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        Set<String> commands = new HashSet<>();
        Set<String> roots = new HashSet<>();

        for (String configuredCommand : plugin.getConfig().getStringList(ALLOWED_COMMANDS_PATH)) {
            String normalizedCommand = normalizeCommand(configuredCommand);
            if (normalizedCommand.isEmpty()) {
                continue;
            }

            commands.add(normalizedCommand);
            if (!normalizedCommand.contains(" ")) {
                roots.add(normalizedCommand);
            }
        }

        this.allowedCommands = Set.copyOf(commands);
        this.allowedRootCommands = Set.copyOf(roots);
    }

    public boolean isCommandAllowed(String command) {
        String normalizedCommand = normalizeCommand(command);
        if (normalizedCommand.isEmpty()) {
            return true;
        }

        if (allowedCommands.contains("*")
            || allowedCommands.contains(normalizedCommand)
            || allowedRootCommands.contains(getRootCommand(normalizedCommand))
        ) {
            return true;
        }

        for (String allowedCommand : allowedCommands) {
            if (normalizedCommand.startsWith(allowedCommand + " ")) {
                return true;
            }
        }

        return false;
    }

    private String normalizeCommand(String command) {
        if (command == null) {
            return "";
        }

        String trimmedCommand = command.trim();
        while (trimmedCommand.startsWith("/")) {
            trimmedCommand = trimmedCommand.substring(1).trim();
        }

        if (trimmedCommand.isEmpty()) {
            return "";
        }

        String[] parts = trimmedCommand.toLowerCase(Locale.ENGLISH).split("\\s+");
        parts[0] = stripNamespace(parts[0]);

        return String.join(" ", parts);
    }

    private String getRootCommand(String normalizedCommand) {
        int firstSpace = normalizedCommand.indexOf(' ');

        if (firstSpace == -1) {
            return normalizedCommand;
        }

        return normalizedCommand.substring(0, firstSpace);
    }

    private String stripNamespace(String command) {
        int namespaceSeparator = command.indexOf(':');

        if (namespaceSeparator == -1) {
            return command;
        }

        return command.substring(namespaceSeparator + 1);
    }
}

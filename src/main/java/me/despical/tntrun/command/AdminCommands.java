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

package me.despical.tntrun.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.annotations.Command;
import me.despical.commandframework.annotations.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.string.StringMatcher;
import me.despical.commons.util.Strings;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import me.despical.tntrun.user.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 2.02.2023
 */
public class AdminCommands extends AbstractCommand {

    @Command(
        name = "tntrun",
        usage = "/tntrun",
        desc = "Main command of the plugin."
    )
    public void mainCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage("&3This server is running &bTNT Run {0} &3by &bDespical&3.", plugin.getDescription().getVersion());

            if (arguments.hasPermission("tntrun.admin")) {
                arguments.sendMessage("&3Commands: &b/{0} help", arguments.getLabel());
            }

            return;
        }

        var commandFramework = plugin.getCommandFramework();
        String label = arguments.getLabel(), arg = arguments.getArgument(0);
        List<String> commands = commandFramework.getSubCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList());
        List<StringMatcher.Match> matches = StringMatcher.match(arg, commands);

        if (!matches.isEmpty()) {
            Optional<Command> optionalMatch = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(label + "." + matches.get(0).getMatch())).findFirst();

            if (optionalMatch.isPresent()) {
                String matchedName = getMatchingParts(optionalMatch.get().name(), label + "." + String.join(".", arguments.getArguments()));
                Optional<Command> matchedCommand = commandFramework.getSubCommands().stream().filter(cmd -> cmd.name().equals(matchedName)).findFirst();

                if (matchedCommand.isPresent()) {
                    arguments.sendMessage(chatManager.message("admin-commands.correct-usage").replace("%usage%", matchedCommand.get().usage()));
                    return;
                }

                arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", optionalMatch.get().usage()));
                return;
            }

            arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", "/" + label));
        }
    }

    @Command(
        name = "tntrun.create",
        permission = "tntrun.admin.create",
        desc = "Creates an arena instance with the given ID.",
        usage = "/tntrun create <arena name>",
        senderType = Command.SenderType.PLAYER
    )
    public void createCommand(User user, CommandArguments arguments) {
        if (plugin.getArenaRegistry().isInArena(user)) {
            user.sendMessage("admin-commands.cannot-do-that-ingame");
            return;
        }

        if (arguments.isArgumentsEmpty()) {
            user.sendMessage("admin-commands.provide-an-arena-name");
            return;
        }

        final var id = arguments.getArgument(0);

        if (plugin.getArenaRegistry().isArena(id)) {
            user.sendMessage("admin-commands.there-is-already-an-arena");
            return;
        }

        final var path = "instance.%s.".formatted(id);
        final var arena = new Arena(id);

        plugin.getArenaRegistry().registerArena(arena);

        final var config = ConfigUtils.getConfig(plugin, "arena");
        config.set(path + "ready", false);
        config.set(path + "mapName", id);
        config.set(path + "minimumPlayers", 2);
        config.set(path + "maximumPlayers", 12);
        config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
        config.set(path + "lobbyLocation", LocationSerializer.SERIALIZED_LOCATION);
        config.set(path + "signs", Collections.EMPTY_LIST);

        ConfigUtils.saveConfig(plugin, config, "arena");

        final var player = user.getPlayer();

        user.sendRawMessage("&3&l--------------------------------------------");
        MiscUtils.sendCenteredMessage(player, "&bArena " + id + " created!");
        user.sendRawMessage("");
        MiscUtils.sendCenteredMessage(player, "&bEdit this arena via &3/tntrun edit " + id + "&b!");
        user.sendRawMessage("&3&l--------------------------------------------");
    }

    @Command(
        name = "tntrun.delete",
        permission = "tntrun.admin.delete",
        desc = "Deletes the arena instance with the given ID, if it exists.",
        usage = "/tntrun delete <arena name>",
        senderType = Command.SenderType.PLAYER
    )
    public void deleteCommand(User user, CommandArguments arguments) {
        if (plugin.getArenaRegistry().isInArena(user)) {
            user.sendMessage("admin-commands.cannot-do-that-ingame");
            return;
        }

        if (arguments.isArgumentsEmpty()) {
            user.sendRawMessage("admin-commands.provide-an-arena-name");
            return;
        }

        final var arenaId = arguments.getArgument(0);

        if (!plugin.getArenaRegistry().isArena(arenaId)) {
            user.sendMessage("admin-commands.no-arena-found-with-that-name");
            return;
        }

        final var arena = plugin.getArenaRegistry().getArena(arenaId);
        arena.stop();

        final var config = ConfigUtils.getConfig(plugin, "arena");
        config.set("instance." + arenaId, null);
        ConfigUtils.saveConfig(plugin, config, "arena");

        plugin.getSignManager().removeArenaSigns(arena);
        plugin.getArenaRegistry().unregisterArena(arena);

        user.sendMessage("admin-commands.deleted-arena-successfully", arena);
    }

    @Command(
        name = "tntrun.list",
        permission = "tntrun.admin.list",
        desc = "Shows the list of existing arena IDs.",
        usage = "/tntrun list",
        senderType = Command.SenderType.PLAYER
    )
    public void listCommand(User user, CommandArguments arguments) {
        final var arenas = plugin.getArenaRegistry().getArenas();

        if (arenas.isEmpty()) {
            user.sendMessage("admin-commands.list-command.no-arenas-created");
            return;
        }

        final var list = arenas.stream().map(Arena::getId).collect(Collectors.joining(", "));
        arguments.sendMessage(chatManager.message("admin-commands.list-command.format").replace("%list%", list));
    }

    @Command(
        name = "tntrun.forcestart",
        permission = "tntrun.admin.forcestart",
        desc = "Forces the arena that the player is in to start, if there are enough players.",
        usage = "/tntrun forcestart",
        senderType = Command.SenderType.PLAYER
    )
    public void forceStartCommand(User user) {
        if (!user.isInArena()) {
            user.sendMessage("admin-commands.must-be-in-arena");
            return;
        }

        final var arena = user.getArena();

        if (arena.getPlayers().size() < 2) {
            arena.broadcastMessage("messages.arena.waiting-for-players");
            return;
        }

        if (arena.isForceStart()) {
            user.sendMessage("messages.in-game.already-force-start");
            return;
        }

        if (arena.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING)) {
            arena.setArenaState(ArenaState.STARTING);
            arena.setForceStart(true);
            arena.setTimer(0);
            arena.getPlayers().forEach(u -> u.sendMessage("messages.in-game.force-start"));
        }
    }

    @Command(
        name = "tntrun.stop",
        permission = "tntrun.admin.stop",
        desc = "Forces the arena that the player is in to stop.",
        usage = "/tntrun stop",
        senderType = Command.SenderType.PLAYER
    )
    public void stopCommand(User user) {
        final var arena = user.getArena();

        if (arena == null) {
            user.sendMessage("admin-commands.must-be-in-arena");
            return;
        }

        if (arena.getArenaState() != ArenaState.ENDING) {
            plugin.getArenaManager().stopGame(true, arena);
        }
    }

    @Command(
        name = "tntrun.edit",
        permission = "tntrun.admin.edit",
        desc = "Open arena editor for specified arena",
        usage = "/tntrun edit <arena name>",
        senderType = Command.SenderType.PLAYER
    )
    public void editCommand(User user, CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            user.sendMessage("admin-commands.provide-an-arena-name");
            return;
        }

        final var arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

        if (arena == null) {
            user.sendMessage("admin-commands.no-arena-found-with-that-name");
            return;
        }

        new ArenaEditorGUI(plugin, user, arena).showGui();
    }

    @Command(
        name = "tntrun.reload",
        permission = "tntrun.admin.reload",
        desc = "Reloads the configuration files and arenas.",
        usage = "/tntrun reload",
        senderType = Command.SenderType.PLAYER
    )
    public void reloadCommand(User user) {
        plugin.reloadConfig();
        chatManager.reload();

        plugin.getSignManager().loadSigns();
        plugin.getRewardsFactory().reload();

        user.sendMessage("admin-commands.system-reloaded");
    }

    @SuppressWarnings("deprecation")
    @Command(
        name = "tntrun.help",
        usage = "/tntrun help",
        desc = "Displays a list of available commands along with their descriptions.",
        permission = "tntrun.admin.help"
    )
    public void helpCommand(CommandArguments arguments) {
        final var isPlayer = arguments.isSenderPlayer();
        final var sender = arguments.getSender();

        arguments.sendMessage("");
        MiscUtils.sendCenteredMessage(sender, "&3&lTNT Run");
        MiscUtils.sendCenteredMessage(sender, "&3[&boptional argument&3] &b- &3<&brequired argument&3>");
        arguments.sendMessage("");

        for (final var command : plugin.getCommandFramework().getSubCommands()) {
            final String usage = formatCommandUsage("&3" + command.usage()), desc = command.desc();

            if (desc.isEmpty()) continue;

            if (isPlayer) {
                ((Player) sender).spigot().sendMessage(
                    new ComponentBuilder(ChatColor.DARK_GRAY + " • ")
                        .append(usage)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.usage()))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
                        .color(ChatColor.AQUA)
                        .create());
            } else {
                arguments.sendMessage(" &8• &b" + usage + " &3- &b" + desc);
            }
        }

        if (isPlayer) {
            Player player = arguments.getSender();
            player.sendMessage("");
            player.spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
                .append(" Try to ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .append("hover").color(ChatColor.WHITE).underlined(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Hover on the commands to get info about them.")))
                .append(" or ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .append("click").color(ChatColor.WHITE).underlined(true)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.LIGHT_PURPLE + "Click on the commands to insert them in the chat.")))
                .append(" on the commands!", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
                .create());
        }
    }

    @Completer(
        name = "tntrun"
    )
    public List<String> onTabComplete(CommandArguments arguments) {
        final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getSubCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
        final String args[] = arguments.getArguments(), arg = args[0];

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(arg, arguments.hasPermission("tntrun.admin") || arguments.getSender().isOp() ? commands : List.of("top", "stats", "join", "leave", "randomjoin"), completions);
        }

        if (args.length == 2) {
            if (List.of("create", "list", "randomjoin", "leave", "reload").contains(arg)) return completions;

            if (arg.equalsIgnoreCase("top")) {
                return StringUtil.copyPartialMatches(
                    args[1],
                    List.of("wins", "loses", "coins", "games_played", "longest_survive"),
                    completions);
            }

            if (arg.equalsIgnoreCase("stats")) {
                return StringUtil.copyPartialMatches(
                    args[1],
                    plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
                    completions
                );
            }

            if (List.of("edit", "delete", "join").contains(arg)) {
                return StringUtil.copyPartialMatches(
                    args[1],
                    plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList()),
                    completions);
            }
        }

        return completions;
    }

    private String getMatchingParts(String matched, String current) {
        String[] matchedArray = matched.split("\\."), currentArray = current.split("\\.");
        int max = Math.min(matchedArray.length, currentArray.length);
        List<String> matchingParts = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            if (matchedArray[i].equals(currentArray[i])) {
                matchingParts.add(matchedArray[i]);
            }
        }

        return String.join(".", matchingParts);
    }

    private String formatCommandUsage(String usage) {
        final var array = usage.toCharArray();
        final var buffer = new StringBuilder(usage);

        for (int i = 0; i < array.length; i++) {
            if (array[i] == '[' || array[i] == '<') {
                buffer.insert(i, "&b");
                return Strings.format(buffer.toString());
            }
        }

        return Strings.format(usage);
    }
}

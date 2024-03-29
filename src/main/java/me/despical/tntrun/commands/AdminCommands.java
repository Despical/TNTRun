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

package me.despical.tntrun.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.Completer;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.string.StringMatcher;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static me.despical.commandframework.Command.SenderType.PLAYER;

/**
 * @author Despical
 * <p>
 * Created at 2.02.2023
 */
public class AdminCommands extends AbstractCommand {

	public AdminCommands(Main plugin) {
		super(plugin);
		this.plugin.getCommandFramework().setMatchFunction(arguments -> {
			if (arguments.isArgumentsEmpty()) return false;

			String label = arguments.getLabel(), arg = arguments.getArgument(0);

			var matches = StringMatcher.match(arg, plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(label + ".", "")).collect(Collectors.toList()));

			if (!matches.isEmpty()) {
				arguments.sendMessage(chatManager.message("admin-commands.did-you-mean").replace("%command%", label + " " + matches.get(0).getMatch()));
				return true;
			}

			return false;
		});
	}

	@Command(
		name = "tntrun",
		usage = "tntrun help",
		desc = "Main command of TNT Run."
	)
	public void mainCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.rawMessage("&3This server is running &bTNT Run " + plugin.getDescription().getVersion() + " &3by &bDespical&3!"));

			if (arguments.hasPermission("tntrun.admin")) {
				arguments.sendMessage(chatManager.rawMessage("&3Commands: &b/" + arguments.getLabel() + " help"));
			}
		}
	}

	@Command(
		name = "tntrun.create",
		permission = "tntrun.admin.create",
		desc = "Create an arena with default configuration.",
		usage = "/tntrun create <arena name>",
		allowInfiniteArgs = true,
		senderType = PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

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
		MiscUtils.sendCenteredMessage(player, "&bEdit this arena via &e/tntrun edit " + id + "&b!");
		user.sendRawMessage("&3&l--------------------------------------------");
	}

	@Command(
		name = "tntrun.delete",
		permission = "tntrun.admin.delete",
		desc = "Delete specified arena and its data",
		usage = "/tntrun delete <arena name>",
		allowInfiniteArgs = true,
		senderType = PLAYER
	)
	public void deleteCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

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
		desc = "Get a list of registered arenas and their status",
		usage = "/tntrun list",
		senderType = PLAYER
	)
	public void listCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
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
		desc = "Forces arena to start without waiting time",
		usage = "/tntrun forcestart",
		senderType = PLAYER
	)
	public void forceStartCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

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
		desc = "Stop the arena that you're in",
		usage = "/tntrun stop",
		senderType = PLAYER
	)
	public void stopCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());
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
		allowInfiniteArgs = true,
		senderType = PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		final var user = plugin.getUserManager().getUser(arguments.getSender());

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

	@SuppressWarnings("deprecation")
	@Command(
		name = "tntrun.help",
		permission = "tntrun.admin.help"
	)
	public void helpCommand(CommandArguments arguments) {
		final var isPlayer = arguments.isSenderPlayer();
		final var sender = arguments.getSender();
		final var message = chatManager.rawMessage("&3&l---- TNT Run Admin Commands ----");

		arguments.sendMessage("");
		MiscUtils.sendCenteredMessage(sender, message);
		arguments.sendMessage("");

		for (final var command : plugin.getCommandFramework().getCommands().stream().sorted(Collections
			.reverseOrder(Comparator.comparingInt(cmd -> cmd.usage().length()))).toList()) {
			String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty() || usage.contains("help")) continue;

			if (isPlayer) {
				((Player) sender).spigot().sendMessage(new ComponentBuilder()
					.color(ChatColor.DARK_GRAY)
					.append(" • ")
					.append(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				sender.sendMessage(chatManager.rawMessage(" &8• &b" + usage + " &3- &b" + desc));
			}
		}

		if (isPlayer) {
			final var player = arguments.getSender();
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
		name = "tntrun",
		aliases = {"tr"}
	)
	public List<String> onTabComplete(CommandArguments arguments) {
		final List<String> completions = new ArrayList<>(), commands = plugin.getCommandFramework().getCommands().stream().map(cmd -> cmd.name().replace(arguments.getLabel() + '.', "")).collect(Collectors.toList());
		final String args[] = arguments.getArguments(), arg = args[0];

		commands.remove("tntrun");

		if (args.length == 1) {
			StringUtil.copyPartialMatches(arg, arguments.hasPermission("tntrun.admin") || arguments.getSender().isOp() ? commands : List.of("top", "stats", "join", "leave", "randomjoin"), completions);
			return completions;
		}

		if (args.length == 2) {
			if (List.of("create", "list", "randomjoin", "leave").contains(arg)) return null;

			if (arg.equalsIgnoreCase("top")) {
				StringUtil.copyPartialMatches(
								args[1],
								List.of("wins", "loses", "coins", "games_played", "longest_survive"),
								completions);
				return completions;
			}

			if (arg.equalsIgnoreCase("stats")) {
				StringUtil.copyPartialMatches(
								args[1],
								plugin.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()),
								completions
				);
				return completions;
			}

			StringUtil.copyPartialMatches(
								args[1],
								plugin.getArenaRegistry().getArenas().stream().map(Arena::getId).collect(Collectors.toList()),
								completions);
			return completions;
		}

		return completions;
	}
}
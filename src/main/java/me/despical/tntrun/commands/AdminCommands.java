package me.despical.tntrun.commands;

import me.despical.commandframework.Command;
import me.despical.commandframework.CommandArguments;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.setup.SetupInventory;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 22.05.2022
 */
public class AdminCommands {

	private final Main plugin;
	private final FileConfiguration config;
	private final ChatManager chatManager;

	private final Set<Player> deleteConfirmations, reloadConfirmations;

	public AdminCommands(Main plugin) {
		this.plugin = plugin;
		this.plugin.getCommandFramework().registerCommands(this);
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.chatManager = plugin.getChatManager();
		this.deleteConfirmations = new HashSet<>();
		this.reloadConfirmations = new HashSet<>();
	}

	@Command(
		name = "tntrun.create",
		permission = "tntrun.admin.create",
		usage = "/tntrun create <arenaId>",
		desc = "Creates a new arena with default configuration",
		senderType = Command.SenderType.PLAYER
	)
	public void createCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.type-arena-name"));
			return;
		}

		String id = arguments.getArgument(0);

		if (ArenaRegistry.isArena(id)) {
			arguments.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already exists! Use another ID or delete it first!"));
			return;
		}

		Player player = arguments.getSender();

		MiscUtils.sendCenteredMessage(player, "&l--------------------------------------------");
		MiscUtils.sendCenteredMessage(player, "Instance " + id + " created!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via &6/tntrun edit " + id + "&a!");
		MiscUtils.sendCenteredMessage(player, "&l--------------------------------------------");

		// Configuration setup and registering arena
		String path = "instances." + id + ".";
		config.set(path + "ready", false);
		config.set(path + "mapName", id);
		config.set(path + "minimumPlayers", 2);
		config.set(path + "maximumPlayers", 12);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "lobbyLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "signs", new ArrayList<>());

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setMapName(id);
		arena.setReady(false);
		arena.setLobbyLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);

		ArenaRegistry.registerArena(arena);
	}

	@Command(
		name = "tntrun.delete",
		permission = "tntrun.admin.delete",
		usage = "/tntrun delete <arena>",
		desc = "Deletes specified arena",
		senderType = Command.SenderType.PLAYER
	)
	public void deleteCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.type-arena-name"));
			return;
		}

		Arena arena = ArenaRegistry.getArena(arguments.getArgument(0));

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no-arena-like-that"));
			return;
		}

		Player player = arguments.getSender();

		if (!deleteConfirmations.contains(player)) {
			deleteConfirmations.add(player);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> deleteConfirmations.remove(player), 200);

			arguments.sendMessage(chatManager.prefixedMessage("commands.are-you-sure"));
			return;
		}

		deleteConfirmations.remove(player);

		ArenaManager.stopGame(true, arena);
		plugin.getSignManager().getArenaSigns().remove(plugin.getSignManager().getArenaSignByArena(arena));
		ArenaRegistry.unregisterArena(arena);

		config.set("instances." + arena.getId(), null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		arguments.sendMessage(chatManager.prefixedMessage("commands.removed-game-instance"));
	}

	@Command(
		name = "tntrun.list",
		permission = "tntrun.admin.list",
		usage = "/tntrun list",
		desc = "Shows all of the existing arenas"
	)
	public void listCommand(CommandArguments arguments) {
		List<String> arenas = ArenaRegistry.getArenas().stream().map(Arena::getId).collect(Collectors.toList());

		if (arenas.isEmpty()) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.admin-commands.list-command.no-arenas-created"));
			return;
		}

		arguments.sendMessage(chatManager.prefixedMessage("commands.admin-commands.list-command.format").replace("%list%", String.join(", ", arenas)));
	}

	@Command(
		name = "tntrun.edit",
		permission = "tntrun.admin.setup",
		usage = "/tntrun edit <arena>",
		desc = "Opens arena editor menu",
		min = 1,
		senderType = Command.SenderType.PLAYER
	)
	public void editCommand(CommandArguments arguments) {
		Arena arena = ArenaRegistry.getArena(arguments.getArgument(0));

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.no-arena-like-that"));
			return;
		}

		new SetupInventory(plugin, arena, arguments.getSender()).openInventory();
	}

	@Command(
		name = "tntrun.forcestart",
		permission = "tntrun.admin.forcestart",
		usage = "/tntrun forcestart",
		desc = "Forces arena to start that you're in",
		senderType = Command.SenderType.PLAYER
	)
	public void forceStartCommand(CommandArguments arguments) {
		Player player = arguments.getSender();
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.not-playing", player));
			return;
		}

		if (arena.getPlayers().size() < 2) {
			arena.broadcastMessage(chatManager.formatMessage(arena, chatManager.message("in-game.messages.lobby-messages.waiting-for-players"), arena.getMinimumPlayers()));
			return;
		}

		if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS) {
			arena.setArenaState(ArenaState.STARTING);
			arena.setForceStart(true);
			arena.setTimer(0);
			arena.broadcastMessage(chatManager.prefixedMessage("in-game.messages.admin-messages.set-starting-in-to-0"));
		}
	}

	@Command(
		name = "tntrun.stop",
		permission = "tntrun.admin.stop",
		usage = "/tntrun stop",
		desc = "Stops the arena you're in",
		senderType = Command.SenderType.PLAYER
	)
	public void stopCommand(CommandArguments arguments) {
		Player player = arguments.getSender();
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			arguments.sendMessage(chatManager.prefixedMessage("commands.not-playing"));
			return;
		}

		if (arena.getArenaState() != ArenaState.ENDING) {
			ArenaManager.stopGame(true, arena);
		}
	}

	@Command(
		name = "tntrun.reload",
		permission = "tntrun.admin.reload",
		usage = "/tntrun reload",
		desc = "Stops arenas then reloads everything",
		senderType = Command.SenderType.PLAYER
	)
	public void reloadCommand(CommandArguments arguments) {
		Player sender = arguments.getSender();

		if (!reloadConfirmations.contains(sender)) {
			reloadConfirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> reloadConfirmations.remove(sender), 200);

			arguments.sendMessage(chatManager.prefixedMessage("Commands.are-you-sure"));
			return;
		}

		reloadConfirmations.remove(sender);
		LogUtils.log("Initiated plugin reload by {0}.", sender.getName());

		long start = System.currentTimeMillis();

		plugin.reloadConfig();
		plugin.getChatManager().reloadConfig();

		// 0dan reload command 0dan arena stateler vs

		for (Arena arena : ArenaRegistry.getArenas()) {
			LogUtils.log("Stopping {0} instance.", arena.getId());

			ArenaManager.stopGame(true, arena);
			ArenaRegistry.unregisterArena(arena);
		}

		ArenaRegistry.registerArenas();

		sender.sendMessage(chatManager.prefixedMessage("Commands.admin-commands.success-reload"));
		LogUtils.log("Finished reloading took {0} ms.", System.currentTimeMillis() - start);
	}

	@Command(
		name = "tntrun.help",
		permission = "tntrun.admin",
		usage = "/tntrun help",
		desc = "Sends all of the command and their usages"
	)
	public void helpCommand(CommandArguments arguments) {
		arguments.sendMessage(chatManager.color("&3&l---- TNT Run Admin Commands ----"));
		arguments.sendMessage("");

		boolean isPlayer = arguments.isSenderPlayer();

		for (Command command : plugin.getCommandFramework().getCommands()) {
			String usage = command.usage(), desc = command.desc();

			if (usage.isEmpty()) continue;

			if (isPlayer) {
				arguments.getSender().spigot().sendMessage(new ComponentBuilder(usage)
					.color(ChatColor.AQUA)
					.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, usage))
					.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(desc)))
					.create());
			} else {
				arguments.sendMessage(chatManager.color("&b" + usage + " &3- &b" + desc));
			}
		}

		if (isPlayer) {
			arguments.sendMessage("");
			arguments.getSender().spigot().sendMessage(new ComponentBuilder("TIP:").color(ChatColor.YELLOW).bold(true)
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
}
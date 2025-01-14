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

package me.despical.tntrun.handlers.sign;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.events.EventListener;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 02.07.2020
 */
public class SignManager extends EventListener {

	private final List<String> signLines;
	private final Set<ArenaSign> arenaSigns;
	private final Map<ArenaState, String> gameStateToString;

	public SignManager(Main plugin) {
		super(plugin);
		this.signLines = plugin.getChatManager().getStringList("signs.lines");
		this.arenaSigns = new HashSet<>();
		this.gameStateToString = new EnumMap<>(ArenaState.class);

		for (var state : ArenaState.values()) {
			gameStateToString.put(state, plugin.getChatManager().message("signs.game-states." + state.getDefaultName().toLowerCase()));
		}

		this.loadSigns();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.hasPermission("tntrun.admin.sign.create") || !"[tntrun]".equalsIgnoreCase(event.getLine(0))) {
			return;
		}

		var line = event.getLine(1);

		if ("".equalsIgnoreCase(line)) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}

		var arena = plugin.getArenaRegistry().getArena(line);

		if (arena == null) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}

		var block = event.getBlock();

		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));

		for (int i = 0; i < signLines.size(); i++) {
			event.setLine(i, formatSign(signLines.get(i), arena));
		}

		user.sendRawMessage("&aArena sign has been created successfully!");

		var config = ConfigUtils.getConfig(plugin, "arena");
		var path = "instance.%s.signs".formatted(arena);
		var locs = config.getStringList(path);
		locs.add(LocationSerializer.toString(event.getBlock().getLocation()));

		config.set(path, locs);
		ConfigUtils.saveConfig(plugin, config, "arena");
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent event) {
		var block = event.getBlock();
		var arenaSign = getArenaSignByBlock(block);

		if (arenaSign == null) return;

		var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.hasPermission("tntrun.admin.sign.break")) {
			event.setCancelled(true);

			user.sendRawMessage("&cYou don't have enough permission to break this sign!");
			return;
		}

		arenaSigns.remove(arenaSign);

		var location = LocationSerializer.toString(block.getLocation());
		var path = "instance.%s.signs".formatted(arenaSign.arena());
		var config = ConfigUtils.getConfig(plugin, "arena");
		var signs = config.getStringList(path);

		for (var loc : signs) {
			if (loc.equals(location)) {
				signs.remove(location);

				config.set(path, signs);
				ConfigUtils.saveConfig(plugin, config, "arena");

				user.sendRawMessage("&aSign removed successfully!");
				return;
			}
		}

		user.sendRawMessage("&cCouldn't remove arena sign! Please do manually!");
	}

	@EventHandler
	public void onJoinAttempt(PlayerInteractEvent event) {
		var arenaSign = getArenaSignByBlock(event.getClickedBlock());

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && arenaSign != null) {
			event.setCancelled(true);

			var arena = arenaSign.arena();

			if (arena == null) return;

			var user = plugin.getUserManager().getUser(event.getPlayer());

			if (user.isInArena()) {
				user.sendMessage("messages.arena.already-playing");
				return;
			}

			plugin.getArenaManager().joinAttempt(user, arena);
		}
	}

	public void loadSigns() {
		arenaSigns.clear();

		var config = ConfigUtils.getConfig(plugin, "arena");
		boolean updateConfig = false, reloadConfig = false;

		for (var path : config.getConfigurationSection("instance").getKeys(false)) {
			var locations = config.getStringList("instance." + path + ".signs");
			var iterator = locations.iterator();

			while (iterator.hasNext()) {
				var location = iterator.next();
				var loc = LocationSerializer.fromString(location);

				if (loc.getBlock().getState() instanceof Sign sign) {
					arenaSigns.add(new ArenaSign(sign, plugin.getArenaRegistry().getArena(path)));
				} else {
					iterator.remove();
					updateConfig = reloadConfig = true;
				}
			}

			if (updateConfig) {
				config.set("instance." + path + ".signs", locations);
				updateConfig = false;
			}
		}

		if (reloadConfig) {
			ConfigUtils.saveConfig(plugin, config, "arena");
		}

		updateSigns();
	}

	public void updateSign(Arena arena) {
		this.arenaSigns.stream().filter(arenaSign -> arenaSign.arena().equals(arena)).forEach(this::updateSign);
	}

	private void updateSign(ArenaSign arenaSign) {
		var sign = arenaSign.sign();

		for (int i = 0; i < signLines.size(); i++) {
			sign.setLine(i, formatSign(signLines.get(i), arenaSign.arena()));
		}

		sign.update();
	}

	public void updateSigns() {
		for (var arenaSign : arenaSigns) {
			var sign = arenaSign.sign();

			for (int i = 0; i < signLines.size(); i++) {
				sign.setLine(i, formatSign(signLines.get(i), arenaSign.arena()));
			}

			sign.update();
		}
	}

	public boolean isGameSign(Block block) {
		return this.arenaSigns.stream().anyMatch(sign -> sign.sign().getLocation().equals(block.getLocation()));
	}

	public void addArenaSign(Block block, Arena arena) {
		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
	}

	public void removeArenaSigns(Arena arena) {
		arenaSigns.removeIf(sign -> sign.arena().equals(arena));
	}

	private String formatSign(String msg, Arena arena) {
		String formatted = msg;
		int size = arena.getPlayers().size(), max = arena.getMaximumPlayers();

		formatted = formatted.replace("%map_name%", arena.getMapName());
		formatted = formatted.replace("%players%", Integer.toString(size));
		formatted = formatted.replace("%max_players%", Integer.toString(max));

		if (size >= max) {
			formatted = formatted.replace("%state%", plugin.getChatManager().message("signs.game-states.full-game"));
		} else {
			formatted = formatted.replace("%state%", gameStateToString.get(arena.getArenaState()));
		}

		return plugin.getChatManager().rawMessage(formatted);
	}

	private ArenaSign getArenaSignByBlock(Block block) {
		return block == null || !(block.getState() instanceof Sign) ? null : arenaSigns.stream().filter(sign -> sign.sign().getLocation().equals(block.getLocation())).findFirst().orElse(null);
	}
}

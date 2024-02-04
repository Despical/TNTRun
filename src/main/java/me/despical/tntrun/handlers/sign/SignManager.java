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
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.logging.Level;

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

		for (final var state : ArenaState.values()) {
			gameStateToString.put(state, plugin.getChatManager().message("signs.game-states." + state.getFormattedName().toLowerCase(java.util.Locale.ENGLISH)));
		}

		this.loadSigns();
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.hasPermission("tntrun.admin.sign.create") || !"[tntrun]".equalsIgnoreCase(event.getLine(0))) {
			return;
		}

		final var line = event.getLine(1);

		if ("".equalsIgnoreCase(line)) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}

		final var arena = plugin.getArenaRegistry().getArena(line);

		if (arena == null) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}

		final var block = event.getBlock();

		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));

		for (int i = 0; i < signLines.size(); i++) {
			event.setLine(i, formatSign(signLines.get(i), arena));
		}

		user.sendRawMessage("&aArena sign has been created successfully!");

		final var config = ConfigUtils.getConfig(plugin, "arena");
		final var path = "instance.%s.signs".formatted(arena);
		final var locs = config.getStringList(path);
		locs.add(LocationSerializer.toString(event.getBlock().getLocation()));

		config.set(path, locs);
		ConfigUtils.saveConfig(plugin, config, "arena");
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent event) {
		final var block = event.getBlock();
		final var arenaSign = getArenaSignByBlock(block);

		if (arenaSign == null) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());

		if (!user.hasPermission("tntrun.admin.sign.break")) {
			event.setCancelled(true);

			user.sendRawMessage("&cYou don't have enough permission to break this sign!");
			return;
		}

		arenaSigns.remove(arenaSign);

		final var location = LocationSerializer.toString(block.getLocation());
		final var path = "instance.%s.signs".formatted(arenaSign.arena());
		final var config = ConfigUtils.getConfig(plugin, "arena");
		final var signs = config.getStringList(path);

		for (final var loc : signs) {
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
		final var arenaSign = getArenaSignByBlock(event.getClickedBlock());

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && arenaSign != null) {
			final var arena = arenaSign.arena();

			if (arena == null) return;

			final var user = plugin.getUserManager().getUser(event.getPlayer());

			if (user.isInArena()) {
				user.sendMessage("messages.arena.already-playing");
				return;
			}

			plugin.getArenaManager().joinAttempt(user, arena);
		}
	}

	public void loadSigns() {
		arenaSigns.clear();

		final var config = ConfigUtils.getConfig(plugin, "arena");

		for (final var arenaId : config.getConfigurationSection("instance").getKeys(false)) {
			for (final var location : config.getStringList("instance." + arenaId + ".signs")) {
				final var loc = LocationSerializer.fromString(location);

				if (loc.getBlock().getState() instanceof Sign sign) {
					arenaSigns.add(new ArenaSign(sign, plugin.getArenaRegistry().getArena(arenaId)));
				} else {
					plugin.getLogger().log(Level.WARNING, "Block at location {0} for arena {1} is not a sign!", new Object[] { location, arenaId });
				}
			}
		}

		updateSigns();
	}

	public void updateSign(final Arena arena) {
		this.arenaSigns.stream().filter(arenaSign -> arenaSign.arena().equals(arena)).forEach(this::updateSign);
	}

	private void updateSign(final ArenaSign arenaSign) {
		final var sign = arenaSign.sign();

		for (int i = 0; i < signLines.size(); i++) {
			sign.setLine(i, formatSign(signLines.get(i), arenaSign.arena()));
		}

		sign.update();
	}

	public void updateSigns() {
		for (final var arenaSign : arenaSigns) {
			final var sign = arenaSign.sign();

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
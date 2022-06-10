/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.miscellaneous.BlockUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.ChatManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.*;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SignManager implements Listener {

	private final Main plugin;
	private final ChatManager chatManager;
	private final FileConfiguration config;
	
	private final List<String> signLines;
	private final List<ArenaSign> arenaSigns;
	private final Map<ArenaState, String> gameStateToString;

	public SignManager(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.arenaSigns = new ArrayList<>();
		this.signLines = chatManager.getStringList("Signs.Lines");
		this.gameStateToString = new EnumMap<>(ArenaState.class);

		for (ArenaState state : ArenaState.values()) {
			gameStateToString.put(state, chatManager.message("signs.game-states." + state.getFormattedName()));
		}

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();

		if (!player.hasPermission("tntrun.admin.sign.create") || !event.getLine(0).equalsIgnoreCase("[tntrun]")) {
			return;
		}

		String id = event.getLine(1);

		if (id == null || id.isEmpty()) {
			player.sendMessage(chatManager.prefixedMessage("signs.please-type-arena-name"));
			return;
		}

		Arena arena = ArenaRegistry.getArena(id);

		if (arena == null) {
			event.getPlayer().sendMessage(chatManager.prefixedMessage("signs.arena-doesnt-exists"));
			return;
		}

		for (int i = 0; i < signLines.size(); i++) {
			event.setLine(i, formatSign(signLines.get(i), arena));
		}

		arenaSigns.add(new ArenaSign((Sign) event.getBlock().getState(), arena));

		List<String> locations = config.getStringList("instances." + id + ".signs");
		locations.add(LocationSerializer.toString(event.getBlock().getLocation()));

		config.set("instances." + id + ".signs", locations);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		player.sendMessage(chatManager.prefixedMessage("signs.sign-created"));
	}

	private String formatSign(String message, Arena arena) {
		String formatted = message;
		formatted = StringUtils.replace(formatted, "%map_name%", arena.getMapName());
		formatted = StringUtils.replace(formatted, "%state%", arena.getPlayers().size() >= arena.getMinimumPlayers() ? chatManager.message("signs.game-states.full-game") : gameStateToString.get(arena.getArenaState()));
		formatted = StringUtils.replace(formatted, "%players%", Integer.toString(arena.getPlayers().size()));
		formatted = StringUtils.replace(formatted, "%max_players%", Integer.toString(arena.getMaximumPlayers()));
		return chatManager.color(formatted);
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent e) {
		ArenaSign arenaSign = getArenaSignByBlock(e.getBlock());

		if (arenaSign == null) {
			return;
		}

		Player player = e.getPlayer();

		if (!player.hasPermission("tntrun.admin.sign.break")) {
			e.setCancelled(true);
			player.sendMessage(chatManager.prefixedMessage("signs.doesnt-have-permission"));
			return;
		}

		arenaSigns.remove(arenaSign);

		String location = LocationSerializer.toString(e.getBlock().getLocation());

		for (String arena : config.getConfigurationSection("instances").getKeys(false)) {
			for (String sign : config.getStringList("instances." + arena + ".signs")) {
				if (!sign.equals(location)) {
					continue;
				}

				List<String> signs = config.getStringList("instances." + arena + ".signs");
				signs.remove(location);

				config.set("instances." + arena + ".signs", signs);
				ConfigUtils.saveConfig(plugin, config, "arenas");

				player.sendMessage(chatManager.prefixedMessage("Signs.Sign-Removed"));
				return;
			}
		}

		player.sendMessage(chatManager.prefixedRawMessage("&cCouldn't remove sign from configuration! Please do this manually!"));
	}

	@EventHandler
	public void onJoinAttempt(PlayerInteractEvent e) {
		ArenaSign arenaSign = getArenaSignByBlock(e.getClickedBlock());

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && arenaSign != null) {
			Arena arena = arenaSign.getArena();

			if (arena == null) {
				return;
			}

			ArenaManager.joinAttempt(e.getPlayer(), arena);
		}
	}

	private ArenaSign getArenaSignByBlock(Block block) {
		return block == null ? null : arenaSigns.stream().filter(sign -> sign.getSign().getLocation().equals(block.getLocation())).findFirst().orElse(null);
	}

	public ArenaSign getArenaSignByArena(Arena arena) {
		return arena == null ? null : arenaSigns.stream().filter(sign -> sign.getArena().equals(arena)).findFirst().orElse(null);
	}

	public void loadSigns() {
		LogUtils.log("Signs load event started.");
		long start = System.currentTimeMillis();

		arenaSigns.clear();

		for (String path : config.getConfigurationSection("instances").getKeys(false)) {
			for (String sign : config.getStringList("instances." + path + ".signs")) {
				Location location = LocationSerializer.fromString(sign);

				if (location.getBlock().getState() instanceof Sign) {
					arenaSigns.add(new ArenaSign((Sign) location.getBlock().getState(), ArenaRegistry.getArena(path)));
				} else {
					LogUtils.log(Level.WARNING, "Block at location {0} for arena {1} is not a sign!", location, path);
				}
			}
		}

		LogUtils.log("Sign load event finished took {0} ms", System.currentTimeMillis() - start);

		updateSigns();
	}

	public void addArenaSign(Block block, Arena arena) {
		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
		updateSigns();
	}

	public void updateSigns() {
		LogUtils.log("Updating signs.");
		long start = System.currentTimeMillis();

		for (ArenaSign arenaSign : arenaSigns) {
			Sign sign = arenaSign.getSign();

			for (int i = 0; i < signLines.size(); i++) {
				sign.setLine(i, formatSign(signLines.get(i), arenaSign.getArena()));
			}

			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.SIGNS_BLOCK_STATES_ENABLED) && arenaSign.getBehind() != null) {
				Block behind = arenaSign.getBehind();

				try {
					switch (arenaSign.getArena().getArenaState()) {
						case WAITING_FOR_PLAYERS:
							behind.setType(XMaterial.WHITE_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 0);
							break;
						case STARTING:
							behind.setType(XMaterial.YELLOW_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 4);
							break;
						case IN_GAME:
							behind.setType(XMaterial.ORANGE_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 1);
							break;
						case ENDING:
							behind.setType(XMaterial.GRAY_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 7);
							break;
						case RESTARTING:
							behind.setType(XMaterial.BLACK_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 15);
							break;
						case INACTIVE:
							behind.setType(XMaterial.RED_STAINED_GLASS.parseMaterial());

							BlockUtils.setData(behind, (byte) 14);
							break;
						default:
							break;
					}
				} catch (Exception ignored) {}
			}

			sign.update();
		}

		LogUtils.log("Updated signs, took {0} ms.", System.currentTimeMillis() - start);
	}

	public List<ArenaSign> getArenaSigns() {
		return new ArrayList<>(arenaSigns);
	}

	public Map<ArenaState, String> getGameStateToString() {
		return gameStateToString;
	}
}
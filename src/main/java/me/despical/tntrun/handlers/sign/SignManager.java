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

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SignManager implements Listener {

	private final Main plugin;
	private final List<ArenaSign> arenaSigns = new ArrayList<>();
	private final Map<ArenaState, String> gameStateToString = new EnumMap<>(ArenaState.class);
	private final List<String> signLines;

	public SignManager(Main plugin) {
		this.plugin = plugin;

		gameStateToString.put(ArenaState.WAITING_FOR_PLAYERS, plugin.getChatManager().message("Signs.Game-States.Waiting"));
		gameStateToString.put(ArenaState.STARTING, plugin.getChatManager().message("Signs.Game-States.Starting"));
		gameStateToString.put(ArenaState.IN_GAME, plugin.getChatManager().message("Signs.Game-States.In-Game"));
		gameStateToString.put(ArenaState.ENDING, plugin.getChatManager().message("Signs.Game-States.Ending"));
		gameStateToString.put(ArenaState.RESTARTING, plugin.getChatManager().message("Signs.Game-States.Restarting"));
		gameStateToString.put(ArenaState.INACTIVE, plugin.getChatManager().message("Signs.Game-States.Inactive"));

		signLines = plugin.getChatManager().getStringList("Signs.Lines");

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		if (!e.getPlayer().hasPermission("tntrun.admin.sign.create") || !e.getLine(0).equalsIgnoreCase("[tntrun]")) {
			return;
		}

		if (e.getLine(1).isEmpty()) {
			e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Signs.Please-Type-Arena-Name"));
			return;
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (!arena.getId().equalsIgnoreCase(e.getLine(1))) {
				continue;
			}

			for (int i = 0; i < signLines.size(); i++) {
				e.setLine(i, formatSign(signLines.get(i), arena));
			}

			arenaSigns.add(new ArenaSign((Sign) e.getBlock().getState(), arena));
			e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Signs.Sign-Created"));

			String location = LocationSerializer.toString(e.getBlock().getLocation());
			FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
			List<String> locs = config.getStringList("instances." + arena.getId() + ".signs");

			locs.add(location);
			config.set("instances." + arena.getId() + ".signs", locs);
			ConfigUtils.saveConfig(plugin, config, "arenas");
			return;
		}

		e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Signs.Arena-Doesnt-Exists"));
	}

	private String formatSign(String msg, Arena a) {
		String formatted = msg;
		formatted = StringUtils.replace(formatted, "%mapname%", a.getMapName());

		if (a.getPlayers().size() >= a.getMaximumPlayers()) {
			formatted = StringUtils.replace(formatted, "%state%", plugin.getChatManager().message("Signs.Game-States.Full-Game"));
		} else {
			formatted = StringUtils.replace(formatted, "%state%", gameStateToString.get(a.getArenaState()));
		}

		formatted = StringUtils.replace(formatted, "%players%", String.valueOf(a.getPlayers().size()));
		formatted = StringUtils.replace(formatted, "%maxplayers%", String.valueOf(a.getMaximumPlayers()));
		formatted = plugin.getChatManager().color(formatted);
		return formatted;
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent e) {
		ArenaSign arenaSign = getArenaSignByBlock(e.getBlock());

		if (arenaSign == null) {
			return;
		}

		if (!e.getPlayer().hasPermission("tntrun.admin.sign.break")) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Signs.Doesnt-Have-Permission"));
			return;
		}

		arenaSigns.remove(arenaSign);

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
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

				e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().message("Signs.Sign-Removed"));
				return;
			}
		}

		e.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + ChatColor.RED + "Couldn't remove sign from configuration! Please do this manually!");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onJoinAttempt(PlayerInteractEvent e) {
		ArenaSign arenaSign = getArenaSignByBlock(e.getClickedBlock());

		if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getClickedBlock().getState() instanceof Sign && arenaSign != null) {
			Arena arena = arenaSign.getArena();

			if (arena == null) {
				return;
			}

			ArenaManager.joinAttempt(e.getPlayer(), arena);
		}
	}

	private ArenaSign getArenaSignByBlock(Block block) {
		if (block == null) {
			return null;
		}

		for (ArenaSign sign : arenaSigns) {
			if (sign.getSign().getLocation().equals(block.getLocation())) {
				return sign;
			}
		}

		return null;
	}

	public ArenaSign getArenaSignByArena(Arena arena) {
		if (arena == null) {
			return null;
		}

		for (ArenaSign sign : arenaSigns) {
			if (sign.getArena() == arena) {
				return sign;
			}
		}

		return null;
	}

	public void loadSigns() {
		LogUtils.log("Signs load event started");
		long start = System.currentTimeMillis();

		arenaSigns.clear();

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		for (String path : config.getConfigurationSection("instances").getKeys(false)) {
			for (String sign : config.getStringList("instances." + path + ".signs")) {
				Location loc = LocationSerializer.fromString(sign);

				if (loc.getBlock().getState() instanceof Sign) {
					arenaSigns.add(new ArenaSign((Sign) loc.getBlock().getState(), ArenaRegistry.getArena(path)));
				} else {
					LogUtils.log(Level.WARNING, "Block at location {0} for arena {1} not a sign", loc, path);
				}
			}
		}

		LogUtils.log("Sign load event finished took {0} ms", System.currentTimeMillis() - start);
	}

	public void addArenaSign(Block block, Arena arena) {
		arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
		updateSigns();
	}

	public void updateSigns() {
		Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			for (ArenaSign arenaSign : arenaSigns) {
				if (arenaSign.getArena() == null) {
					arenaSigns.remove(arenaSign);
					continue;
				}

				Sign sign = arenaSign.getSign();

				for (int i = 0; i < signLines.size(); i++) {
					sign.setLine(i, formatSign(signLines.get(i), arenaSign.getArena()));
				}

				if (plugin.getConfig().getBoolean("Signs-Block-States-Enabled", true) && arenaSign.getBehind() != null) {
					Block behind = arenaSign.getBehind();

					try {
						switch (arenaSign.getArena().getArenaState()) {
							case WAITING_FOR_PLAYERS:
								behind.setType(XMaterial.WHITE_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 0);
								}
								break;
							case STARTING:
								behind.setType(XMaterial.YELLOW_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 4);
								}

								break;
							case IN_GAME:
								behind.setType(XMaterial.ORANGE_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 1);
								}

								break;
							case ENDING:
								behind.setType(XMaterial.GRAY_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 7);
								}

								break;
							case RESTARTING:
								behind.setType(XMaterial.BLACK_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 15);
								}

								break;
							case INACTIVE:
								behind.setType(XMaterial.RED_STAINED_GLASS.parseMaterial());

								if (VersionResolver.isCurrentLower(VersionResolver.ServerVersion.v1_13_R1)) {
									Block.class.getMethod("setData", byte.class).invoke(behind, (byte) 14);
								}
								break;
							default:
								break;
						}
					} catch (Exception ignored) {}
				}

				sign.update();
			}
		}, 10, 10);
	}

	public List<ArenaSign> getArenaSigns() {
		return arenaSigns;
	}

	public Map<ArenaState, String> getGameStateToString() {
		return gameStateToString;
	}
}
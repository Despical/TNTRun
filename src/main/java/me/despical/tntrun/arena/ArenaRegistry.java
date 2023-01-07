/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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

package me.despical.tntrun.arena;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.LogUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaRegistry {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final List<Arena> arenas = new ArrayList<>();

	public static boolean isInArena(Player player) {
		return getArena(player) != null;
	}

	public static boolean isArena(String id) {
		return getArena(id) != null;
	}

	public static Arena getArena(Player player) {
		return arenas.stream().filter(arena -> arena.getPlayers().contains(player)).findFirst().orElse(null);
	}

	public static Arena getArena(String id) {
		return arenas.stream().filter(arena -> arena.getId().equalsIgnoreCase(id)).findFirst().orElse(null);
	}

	public static void registerArena(Arena arena) {
		LogUtils.log("Registering new game instance with id {0}.", arena.getId());
		arenas.add(arena);
	}

	public static void unregisterArena(Arena arena) {
		LogUtils.log("Unregistering game instance with id {0}.", arena.getId());
		arenas.remove(arena);
	}

	public static void registerArenas() {
		LogUtils.log("Initial arenas registration.");
		long start = System.currentTimeMillis();

		arenas.clear();

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
		ChatManager chatManager = plugin.getChatManager();

		if (!config.contains("instances")) {
			LogUtils.sendConsoleMessage(chatManager.message("validator.no-instances-created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			LogUtils.sendConsoleMessage(chatManager.message("validator.no-instances-created"));
			return;
		}

		for (String id : section.getKeys(false)) {
			String path = "instances." + id + ".";
			Arena arena;

			if (path.contains("default")) {
				continue;
			}

			arena = new Arena(id);
			arena.setReady(true);
			arena.setMinimumPlayers(config.getInt(path + "minimumPlayers", 2));
			arena.setMaximumPlayers(config.getInt(path + "maximumPlayers", 10));
			arena.setMapName(config.getString(path + "mapName", "undefined"));
			arena.setLobbyLocation(LocationSerializer.fromString(config.getString(path + "lobbyLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));

			registerArena(arena);

			if (!config.getBoolean(path + "ready", false)) {
				LogUtils.sendConsoleMessage(chatManager.message("validator.invalid-arena-configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				arena.setReady(false);
				continue;
			}

			arena.start();

			LogUtils.sendConsoleMessage(chatManager.message("validator.instance-started").replace("%arena%", id));
		}

		LogUtils.log("Arenas registration completed, took {0} ms.", System.currentTimeMillis() - start);
	}

	public static List<Arena> getArenas() {
		return arenas;
	}
}
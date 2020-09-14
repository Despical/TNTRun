package me.despical.tntrun.arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.tntrun.Main;
import static me.despical.tntrun.utils.Debugger.debug;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaRegistry {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static final List<Arena> arenas = new ArrayList<>();
	private static int bungeeArena = -999;

	/**
	 * Checks if player is in any arena
	 *
	 * @param player player to check
	 * @return [b]true[/b] when player is in arena, [b]false[/b] if otherwise
	 */
	public static boolean isInArena(Player player) {
		for (Arena arena : arenas) {
			if (arena.getPlayers().contains(player)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns arena where the player is
	 *
	 * @param p target player
	 * @return Arena or null if not playing
	 * @see #isInArena(Player) to check if player is playing
	 */
	public static Arena getArena(Player p) {
		if (p == null || !p.isOnline()) {
			return null;
		}

		for (Arena arena : arenas) {
			for (Player player : arena.getPlayers()) {
				if (player.getUniqueId().equals(p.getUniqueId())) {
					return arena;
				}
			}
		}

		return null;
	}

	/**
	 * Returns arena based by ID
	 *
	 * @param id name of arena
	 * @return Arena or null if not found
	 */
	public static Arena getArena(String id) {
		Arena arena = null;

		for (Arena loopArena : arenas) {
			if (loopArena.getId().equalsIgnoreCase(id)) {
				arena = loopArena;
				break;
			}
		}

		return arena;
	}

	public static void registerArena(Arena arena) {
		debug(Level.INFO, "Registering new game instance {0}", arena.getId());
		arenas.add(arena);
	}

	public static void unregisterArena(Arena arena) {
		debug(Level.INFO, "Unregistering game instance {0}", arena.getId());
		arenas.remove(arena);
	}

	public static void registerArenas() {
		debug(Level.INFO, "Initial arenas registration");
		long start = System.currentTimeMillis();

		if (ArenaRegistry.getArenas().size() > 0) {
			for (Arena arena : new ArrayList<>(ArenaRegistry.getArenas())) {
				unregisterArena(arena);
			}
		}

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		if (!config.contains("instances")) {
			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.No-Instances-Created"));
			return;
		}

		ConfigurationSection section = config.getConfigurationSection("instances");

		if (section == null) {
			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.No-Instances-Created"));
			return;
		}

		for (String id : section.getKeys(false)) {
			Arena arena;
			String s = "instances." + id + ".";

			if (s.contains("default")) {
				continue;
			}

			arena = new Arena(id);
			arena.setReady(true);
			arena.setMinimumPlayers(config.getInt(s + "minimumplayers", 2));
			arena.setMaximumPlayers(config.getInt(s + "maximumplayers", 10));
			arena.setMapName(config.getString(s + "mapname", "undefined"));
			arena.setLobbyLocation(LocationSerializer.locationFromString(config.getString(s + "lobbylocation", "world, -994.000, 4.000, 853.000, 0.000, 0.000")));
			arena.setEndLocation(LocationSerializer.locationFromString(config.getString(s + "Endlocation", "world, -994.000, 4.000, 853.000, 0.000, 0.000")));

			if (!config.getBoolean(s + "isdone", false)) {
				Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.Invalid-Arena-Configuration").replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
				arena.setReady(false);
				ArenaRegistry.registerArena(arena);
				continue;
			}

			arena.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
			ArenaRegistry.registerArena(arena);
			arena.start();

			Bukkit.getConsoleSender().sendMessage(plugin.getChatManager().colorMessage("Validator.Instance-Started").replace("%arena%", id));
		}

		debug(Level.INFO, "Arenas registration completed, took {0} ms", System.currentTimeMillis() - start);
	}

	public static List<Arena> getArenas() {
		return arenas;
	}

	public static void shuffleBungeeArena() {
		bungeeArena = new Random().nextInt(arenas.size());
	}

	public static int getBungeeArena() {
		if (bungeeArena == -999) {
			bungeeArena = new Random().nextInt(arenas.size());
		}

		return bungeeArena;
	}
}
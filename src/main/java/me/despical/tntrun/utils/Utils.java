package me.despical.tntrun.utils;

import org.bukkit.entity.Player;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Utils {

	private static Main plugin;

	private Utils() {}

	public static void init(Main plugin) {
		Utils.plugin = plugin;
	}

	/**
	 * Serialize int to use it in Inventories size ex. you have 38 kits and it will
	 * serialize it to 45 (9*5) because it is valid inventory size next ex. you have
	 * 55 items and it will serialize it to 63 (9*7) not 54 because it's too less
	 *
	 * @param i integer to serialize
	 * @return serialized number
	 */
	public static int serializeInt(Integer i) {
		if ((i % 9) == 0) {
			return i;
		} else {
			return (int) ((Math.ceil(i / 9) * 9) + 9);
		}
	}

	public static boolean checkIsInGameInstance(Player player) {
		if (ArenaRegistry.getArena(player) == null) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Not-Playing", player));
			return false;
		}
		return true;
	}
}
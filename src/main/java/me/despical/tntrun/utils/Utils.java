package me.despical.tntrun.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaRegistry;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.concurrent.CompletableFuture;

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

	public static boolean checkIsInGameInstance(Player player) {
		if (ArenaRegistry.isInArena(player)) {
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Not-Playing", player));
			return false;
		}

		return true;
	}

	public static SkullMeta setPlayerHead(Player player, SkullMeta meta) {
		if (Bukkit.getServer().getVersion().contains("Paper") && player.getPlayerProfile().hasTextures()) {
			return CompletableFuture.supplyAsync(() -> {
				meta.setPlayerProfile(player.getPlayerProfile());
				return meta;
			}).exceptionally(e -> {
				Debugger.debug(java.util.logging.Level.WARNING, "Retrieving player profile of " + player.getName() + " failed!");
				return meta;
			}).join();
		}

		meta.setOwningPlayer(player);
		return meta;
	}
}
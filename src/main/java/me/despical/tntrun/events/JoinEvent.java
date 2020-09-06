package me.despical.tntrun.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.handlers.PermissionsManager;
import me.despical.tntrun.utils.UpdateChecker;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class JoinEvent implements Listener {

	private final Main plugin;

	public JoinEvent(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED) && !plugin.getServer().hasWhitelist() || e.getResult() != PlayerLoginEvent.Result.KICK_WHITELIST) {
			return;
		}
		if (e.getPlayer().hasPermission(PermissionsManager.getJoinFullGames())) {
			e.setResult(PlayerLoginEvent.Result.ALLOWED);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		plugin.getUserManager().loadStatistics(plugin.getUserManager().getUser(event.getPlayer()));
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			ArenaRegistry.getArenas().get(ArenaRegistry.getBungeeArena()).teleportToLobby(event.getPlayer());
			return;
		}
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			if (ArenaRegistry.getArena(player) == null) {
				continue;
			}
			player.hidePlayer(plugin, event.getPlayer());
			event.getPlayer().hidePlayer(plugin, player);
		}
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, event.getPlayer());
		}
	}
	
	@EventHandler
	public void onJoinCheckVersion(final PlayerJoinEvent event) {
		if (!plugin.getConfig().getBoolean("Update-Notifier.Enabled", true) || !event.getPlayer().hasPermission("tntrun.updatenotify")) {
			return;
		}
		Bukkit.getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 1).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) {
				return;
			}
			if (result.getNewestVersion().contains("b")) {
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3[TNT Run] &bFound a beta update: v" + result.getNewestVersion() + " Download"));
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3>> &bhttps://www.spigotmc.org/resources/tnt-run-1-12-1-16-2.83196/"));
			} else {
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3[TNT Run] &bFound an update: v" + result.getNewestVersion() + " Download:"));
				event.getPlayer().sendMessage(plugin.getChatManager().colorRawMessage("&3>> &bhttps://www.spigotmc.org/resources/tnt-run-1-12-1-16-2.83196/"));
			}
		}), 25);
	}
}
package me.despical.tntrun.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.user.User;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class QuitEvent implements Listener {

	private final Main plugin;

	public QuitEvent(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			ArenaManager.leaveAttempt(event.getPlayer(), ArenaRegistry.getArena(event.getPlayer()));
		}
		final User user = plugin.getUserManager().getUser(event.getPlayer());
		plugin.getUserManager().removeUser(user);
	}
}
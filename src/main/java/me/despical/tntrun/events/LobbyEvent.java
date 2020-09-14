package me.despical.tntrun.events;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class LobbyEvent implements Listener {

	public LobbyEvent(Main plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onLobbyDamage(EntityDamageEvent event) {
		if (event.getEntity().getType() != EntityType.PLAYER) {
			return;
		}

		Player player = (Player) event.getEntity();
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			return;
		}

		if (event.getDamage() < 500d) {
			event.setCancelled(true);
			player.setFireTicks(0);
		}
	}
}
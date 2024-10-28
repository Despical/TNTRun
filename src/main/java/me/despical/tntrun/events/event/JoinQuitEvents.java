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

package me.despical.tntrun.events.event;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class JoinQuitEvents extends EventListener {

	private final Map<UUID, Arena> teleportToEnd;

	public JoinQuitEvents(Main plugin) {
		super(plugin);
		this.teleportToEnd = new HashMap<>();
	}

	@EventHandler
	public void onJoinEvent(PlayerJoinEvent event) {
		Player eventPlayer = event.getPlayer();

		plugin.getUserManager().addUser(eventPlayer);

		Arena arena = teleportToEnd.get(eventPlayer.getUniqueId());

		if (arena != null) {
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				eventPlayer.teleport(arena.getEndLocation());

				teleportToEnd.remove(eventPlayer.getUniqueId());
			}, 1L);
		}

		for (User user : plugin.getUserManager().getUsers()) {
			if (!user.isInArena()) continue;

			Player player = user.getPlayer();

			eventPlayer.hidePlayer(plugin, player);
			player.hidePlayer(plugin, eventPlayer);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		this.handleQuit(event.getPlayer());
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		this.handleQuit(event.getPlayer());
	}

	private void handleQuit(Player player) {
		User user = plugin.getUserManager().getUser(player);
		Arena arena = user.getArena();

		if (arena != null) {
			plugin.getArenaManager().leaveAttempt(user, arena);

			teleportToEnd.put(player.getUniqueId(), arena);
		}

		plugin.getUserManager().removeUser(player);
	}
}
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

package me.despical.tntrun.handlers.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2023
 */
public class BungeeManager {

	private final Main plugin;
	private final String motd, hubName;
	private final boolean motdEnabled, shutdownWhenGameEnds, connectToHub;
	private final Map<ArenaState, String> gameStates;

	public BungeeManager(Main plugin) {
		this.plugin = plugin;
		this.gameStates = new EnumMap<>(ArenaState.class);

		final var config = ConfigUtils.getConfig(plugin, "bungee");

		this.motd = plugin.getChatManager().rawMessage(config.getString("MOTD.Message"));
		this.hubName = config.getString("Hub");
		this.motdEnabled = config.getBoolean("MOTD.Enabled");
		this.shutdownWhenGameEnds = config.getBoolean("Shutdown-When-Game-Ends");
		this.connectToHub = config.getBoolean("Connect-To-Hub");

		for (final var state : ArenaState.values()) {
			gameStates.put(state, plugin.getChatManager().rawMessage(config.getString("MOTD.Game-States." + state.getFormattedName())));
		}

		plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");

		new BungeeEvents(plugin);
	}

	public void connectToHub(final User user) {
		if (!connectToHub) return;

		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(hubName);

		user.getPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}

	public boolean isShutdownWhenGameEnds() {
		return shutdownWhenGameEnds;
	}

	private class BungeeEvents extends EventListener {

		public BungeeEvents(Main plugin) {
			super(plugin);
		}

		@EventHandler
		public void onJoin(PlayerJoinEvent event) {
			final var arenaRegistry = plugin.getArenaRegistry();

			if (arenaRegistry.getArenas().isEmpty()) {
				return;
			}

			event.setJoinMessage("");

			var user = userManager.getUser(event.getPlayer());
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getArenaManager().joinAttempt(user, arenaRegistry.getBungeeArena()), 1L);
		}

		@EventHandler
		public void onQuit(PlayerQuitEvent event) {
			final var arenaRegistry = plugin.getArenaRegistry();

			if (arenaRegistry.getArenas().isEmpty()) {
				return;
			}

			event.setQuitMessage("");

			final var user = userManager.getUser(event.getPlayer());
			final var arena = user.getArena();

			if (arena != null) {
				plugin.getArenaManager().leaveAttempt(user, arena);
			}
		}

		@EventHandler
		public void onServerListPing(ServerListPingEvent event) {
			if (!motdEnabled) {
				return;
			}

			final var arenaRegistry = plugin.getArenaRegistry();

			if (arenaRegistry.getArenas().isEmpty()) {
				return;
			}

			final var bungeeArena = arenaRegistry.getBungeeArena();

			event.setMaxPlayers(bungeeArena.getMaximumPlayers());
			event.setMotd(motd.replace("%state%", gameStates.get(bungeeArena.getArenaState())));
		}
	}
}
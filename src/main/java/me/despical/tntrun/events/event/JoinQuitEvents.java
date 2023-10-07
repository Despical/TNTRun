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

package me.despical.tntrun.events.event;

import me.despical.commons.util.UpdateChecker;
import me.despical.tntrun.Main;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class JoinQuitEvents extends EventListener {

	public JoinQuitEvents(Main plugin) {
		super(plugin);
	}

	@EventHandler
	public void onJoinEvent(final PlayerJoinEvent event) {
		final var eventPlayer = event.getPlayer();
		final var user = plugin.getUserManager().getUser(eventPlayer);

		plugin.getUserManager().loadStatistics(user);

		this.checkForUpdates(user);

		for (final var targetUser : plugin.getUserManager().getUsers()) {
			if (!targetUser.isInArena()) continue;

			final var player = targetUser.getPlayer();

			eventPlayer.hidePlayer(plugin, player);
			player.hidePlayer(plugin, eventPlayer);
		}
	}

	@EventHandler
	public void onQuitEvent(final PlayerQuitEvent event) {
		this.handleQuitEvent(event.getPlayer());
	}

	@EventHandler
	public void onKickEvent(final PlayerKickEvent event) {
		this.handleQuitEvent(event.getPlayer());
	}

	private void handleQuitEvent(final Player player) {
		final var user = plugin.getUserManager().getUser(player);
		final var arena = user.getArena();

		if (arena != null) {
			plugin.getArenaManager().leaveAttempt(user, arena);
		}

		plugin.getUserManager().removeUser(player);
	}

	private void checkForUpdates(final User user) {
		if (!plugin.getPermissionManager().hasNotifyPerm(user.getPlayer())) return;

		UpdateChecker.init(plugin, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (result.requiresUpdate()) {
				user.sendRawMessage("Found a new version available: v" + result.getNewestVersion());
				user.sendRawMessage("Download it on SpigotMC:");
				user.sendRawMessage("https://spigotmc.org/resources/83196");
			}
		});
	}
}
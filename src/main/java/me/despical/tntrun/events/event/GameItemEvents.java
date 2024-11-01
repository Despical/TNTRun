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

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.events.spectator.SpectatorSettingsGUI;
import me.despical.tntrun.events.spectator.SpectatorTeleporterGUI;
import me.despical.tntrun.user.User;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class GameItemEvents extends EventListener {

	private final Set<User> leaveConfirmations;

	public GameItemEvents(Main plugin) {
		super(plugin);
		this.leaveConfirmations = new HashSet<>();
	}

	@EventHandler
	public void onDoubleJump(PlayerToggleFlightEvent event) {
		final var player = event.getPlayer();

		if (!event.isFlying() && player.getGameMode() != GameMode.ADVENTURE) {
			return;
		}

		final var user = plugin.getUserManager().getUser(player);
		final var arena = user.getArena();

		if (arena == null || user.isSpectator() || arena.isDeathPlayer(user)) {
			return;
		}

		if (user.getCooldown("double_jump") > 0) {
			return;
		}

		if (user.getStat(StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
			event.setCancelled(true);

			user.applyDoubleJumpDelay();

			player.setFlying(false);
			player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
		}
	}

	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();

		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.PHYSICAL) return;

		User user = plugin.getUserManager().getUser(player);

		if (!user.isInArena()) {
			return;
		}

		if (event.getItem() == null) {
			return;
		}

		if (user.getCooldown("double_jump") > 0) {
			return;
		}

		var doubleJumpItem = plugin.getItemManager().getItem("double-jump");

		if (doubleJumpItem == null) {
			return;
		}

		if (doubleJumpItem.equals(event.getItem())) {
			event.setCancelled(true);

			if (user.getStat(StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
				event.setCancelled(true);

				user.applyDoubleJumpDelay();

				player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
				player.setFlying(false);
			}
		}
	}

	@EventHandler
	public void onTeleporterItem(PlayerInteractEvent event) {
		User user = plugin.getUserManager().getUser(event.getPlayer());
		Arena arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		var teleporterItem = plugin.getItemManager().getItem( "teleporter-item");

		if (teleporterItem == null) return;
		if (!teleporterItem.equals(event.getItem())) return;

		new SpectatorTeleporterGUI(plugin, user, arena).showGui();
	}

	@EventHandler
	public void onSettingsItem(PlayerInteractEvent event) {
		User user = plugin.getUserManager().getUser(event.getPlayer());
		Arena arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		var settingsItem = plugin.getItemManager().getItem("settings-item");

		if (settingsItem == null) return;
		if (!settingsItem.equals(event.getItem())) return;

		new SpectatorSettingsGUI(plugin, user, arena).showGui();
	}

	@EventHandler
	public void onPlayAgainItem(PlayerInteractEvent event) {
		User user = plugin.getUserManager().getUser(event.getPlayer());
		Arena arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		var playAgainItem = plugin.getItemManager().getItem("play-again");

		if (playAgainItem == null) return;
		if (!playAgainItem.equals(event.getItem())) return;

		var arenas = plugin.getArenaRegistry().getArenas().stream().filter(a -> a.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING) && a.getPlayers().size() < a.getMaximumPlayers()).toList();

		if (!arenas.isEmpty()) {
			arena.getScoreboardManager().removeScoreboard(user);
			arena.getGameBar().doBarAction(user, 0);
			arena.removeUser(user);

			var newArena = arenas.get(0);

			plugin.getArenaManager().joinAttempt(user, newArena);
			return;
		}

		user.sendMessage("player-commands.no-free-arenas");
	}

	@EventHandler
	public void onForceStartItem(PlayerInteractEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var forceStartItem = plugin.getItemManager().getItem("force-start-item");

		if (forceStartItem == null) return;
		if (!forceStartItem.equals(event.getItem())) return;

		if (arena.getPlayers().size() < 2) {
			arena.broadcastWaitingForPlayers();
			return;
		}

		if (arena.isForceStart()) {
			user.sendMessage("messages.in-game.already-force-start");
			return;
		}

		if (arena.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING)) {
			arena.setArenaState(ArenaState.STARTING);
			arena.setForceStart(true);
			arena.setTimer(0);
			arena.getPlayers().forEach(u -> u.sendMessage("messages.in-game.force-start"));
		}
	}

	@EventHandler
	public void onLeaveItem(PlayerInteractEvent event) {
		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var leaveItem = plugin.getItemManager().getItem("leave-item");

		if (leaveItem == null) return;
		if (!leaveItem.equals(event.getItem())) return;

		if (plugin.getOption(ConfigPreferences.Option.INSTANT_LEAVE)) {
			this.leaveArena(user, arena);
			return;
		}

		if (leaveConfirmations.contains(user)) {
			this.leaveConfirmations.remove(user);

			user.sendMessage("messages.game-items.leave-item.teleport-cancelled");
		} else {
			user.sendMessage("messages.game-items.leave-item.returning-lobby");

			this.leaveConfirmations.add(user);

			new BukkitRunnable() {

				int ticks = 0;

				@Override
				public void run() {
					if (!leaveConfirmations.contains(user)) {
						cancel();
						return;
					}

					if (!arena.isInArena(user)) {
						cancel();
						leaveConfirmations.remove(user);
						return;
					}

					if ((ticks += 2) == 60) {
						cancel();
						leaveArena(user, arena);

						leaveConfirmations.remove(user);
					}
				}
			}.runTaskTimer(plugin, 0, 2);
		}
	}

	private void leaveArena(User user, Arena arena) {
		if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			plugin.getBungeeManager().connectToHub(user);
		} else {
			plugin.getArenaManager().leaveAttempt(user, arena);
		}
	}
}
package me.despical.tntrun.events.event;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.events.spectator.SpectatorSettingsGUI;
import me.despical.tntrun.events.spectator.SpectatorTeleporterGUI;
import me.despical.tntrun.user.User;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

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

		if (!event.isFlying() && player.getGameMode() != GameMode.ADVENTURE) return;

		final var user = plugin.getUserManager().getUser(player);

		if (!user.isInArena()) return;
		if (user.isSpectator()) return;
		if (user.getArena().isDeathPlayer(user)) return;

		if (user.getCooldown("double_jump") > 0) return;

		if (user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
			event.setCancelled(true);

			user.addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
			user.setCooldown("double_jump", plugin.getPermissionManager().getDoubleJumpDelay());

			player.setFlying(false);
			player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
		}
	}

	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		final var player = event.getPlayer();
		final var action = event.getAction();

		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(player);

		if (!user.isInArena()) return;
		if (event.getItem() == null) return;

		if (user.getCooldown("double_jump") > 0) return;

		final var doubleJumpItem = plugin.getGameItemManager().getGameItem("double-jump");

		if (doubleJumpItem == null) return;

		if (event.getItem().getType() == doubleJumpItem.getItemStack().getType()) {
			event.setCancelled(true);

			if (user.getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
				event.setCancelled(true);

				user.addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
				user.setCooldown("double_jump", plugin.getPermissionManager().getDoubleJumpDelay());

				player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
				player.setFlying(false);
			}
		}
	}

	@EventHandler
	public void onSpectatorTeleporterItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var teleporterItem = plugin.getGameItemManager().getGameItem( "teleporter-item");

		if (teleporterItem == null) return;
		if (!event.getItem().getItemMeta().equals(teleporterItem.getItemStack().getItemMeta())) return;

		new SpectatorTeleporterGUI(plugin, user, arena).showGui();
	}

	@EventHandler
	public void onSpectatorSettingsItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var settingsItem = plugin.getGameItemManager().getGameItem("settings-item");

		if (settingsItem == null) return;
		if (!event.getItem().getItemMeta().equals(settingsItem.getItemStack().getItemMeta())) return;

		new SpectatorSettingsGUI(plugin, user, arena).showGui();
	}

	@EventHandler
	public void onPlayAgainItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var leaveItem = plugin.getGameItemManager().getGameItem("play-again");

		if (leaveItem == null) return;
		if (!event.getItem().getItemMeta().equals(leaveItem.getItemStack().getItemMeta())) return;

		final var arenas = plugin.getArenaRegistry().getArenas().stream().filter(a -> a.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING) && a.getPlayers().size() < a.getMaximumPlayers()).toList();

		if (!arenas.isEmpty()) {
			arena.removeUser(user);

			var newArena = arenas.get(0);

			plugin.getArenaManager().joinAttempt(user, newArena);
			return;
		}

		user.sendMessage("player-commands.no-free-arenas");
	}

	@EventHandler
	public void onForceStartItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var leaveItem = plugin.getGameItemManager().getGameItem("force-start-item");

		if (leaveItem == null) return;
		if (!event.getItem().getItemMeta().equals(leaveItem.getItemStack().getItemMeta())) return;

		if (arena.getPlayers().size() < 2) {
			arena.broadcastMessage("messages.arena.waiting-for-players");
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
	public void onLeaveItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final var user = plugin.getUserManager().getUser(event.getPlayer());
		final var arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final var leaveItem = plugin.getGameItemManager().getGameItem("leave-item");

		if (leaveItem == null) return;
		if (!event.getItem().getItemMeta().equals(leaveItem.getItemStack().getItemMeta())) return;

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INSTANT_LEAVE)) {
			this.leaveArena(user, arena);
			return;
		}

		if (leaveConfirmations.contains(user)) {
			this.leaveConfirmations.remove(user);

			user.sendMessage("messages.game-items.leave-item.teleport-cancelled");
		} else {
			user.sendMessage("messages.game-items.leave-item.returning-lobby");

			this.leaveConfirmations.add(user);

			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				if (!this.leaveConfirmations.contains(user)) return;

				this.leaveArena(user, arena);

				this.leaveConfirmations.remove(user);
			}, 60);
		}
	}

	private void leaveArena(User user, Arena arena) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			plugin.getBungeeManager().connectToHub(user);
		} else {
			plugin.getArenaManager().leaveAttempt(user, arena);
		}
	}
}
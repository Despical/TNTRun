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

package me.despical.tntrun.events;

import me.despical.commons.compat.VersionResolver;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.util.UpdateChecker;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.handlers.items.GameItem;
import me.despical.tntrun.user.User;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Events extends ListenerAdapter {

	private final Set<User> leaveConfirmations;

	public Events(Main plugin) {
		super (plugin);
		this.leaveConfirmations = new HashSet<>();

		super.registerIf(bool -> VersionResolver.isCurrentEqualOrHigher(VersionResolver.ServerVersion.v1_9_R1), () -> new Listener() {

			@EventHandler
			public void onItemSwap(PlayerSwapHandItemsEvent e) {
				if (ArenaRegistry.isInArena(e.getPlayer())) {
					e.setCancelled(true);
				}
			}
		});
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		plugin.getUserManager().loadStatistics(plugin.getUserManager().getUser(player));

		for (Player p : plugin.getServer().getOnlinePlayers()) {
			if (!ArenaRegistry.isInArena(p)) continue;

			p.hidePlayer(plugin, player);
			player.hidePlayer(plugin, p);
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
			InventorySerializer.loadInventory(plugin, player);
		}

		if (!plugin.getPermissionManager().hasNotifyPerm(player)) {
			return;
		}

		plugin.getServer().getScheduler().runTaskLater(plugin, () -> UpdateChecker.init(plugin, 83196).requestUpdateCheck().whenComplete((result, exception) -> {
			if (!result.requiresUpdate()) return;

			player.sendMessage(plugin.getChatManager().color("&3[TNT Run] &bFound an update: v" + result.getNewestVersion() + " Download:"));
			player.sendMessage(plugin.getChatManager().color("&3>> &bhttps://www.spigotmc.org/resources/tnt-run.83196/"));
		}), 25);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		User user = plugin.getUserManager().getUser(player);
		Arena arena = user.getArena();

		if (arena != null) {
			ArenaManager.leaveAttempt(player, arena);
		}

		plugin.getUserManager().removeUser(user);
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}

		if (!plugin.getConfig().getBoolean("Block-Commands-In-Game", true)) {
			return;
		}

		for (String msg : plugin.getConfig().getStringList("Whitelisted-Commands")) {
			if (event.getMessage().contains(msg)) {
				return;
			}
		}

		if (event.getPlayer().isOp() || event.getPlayer().hasPermission("tntrun.admin") || event.getPlayer().hasPermission("tntrun.command.bypass")) {
			return;
		}

		if (event.getMessage().startsWith("/tntrun") || event.getMessage().startsWith("/tr") || event.getMessage().contains("leave") || event.getMessage().contains("stats")) {
			return;
		}

		event.setCancelled(true);
		event.getPlayer().sendMessage(plugin.getChatManager().prefixedMessage("In-Game.Only-Command-Ingame-Is-Leave"));
	}

	@EventHandler
	public void onInGameInteract(PlayerInteractEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer()) || event.getClickedBlock() == null) {
			return;
		}

		if (event.getClickedBlock().getType() == XMaterial.PAINTING.parseMaterial() || event.getClickedBlock().getType() == XMaterial.FLOWER_POT.parseMaterial()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInGameBedEnter(PlayerBedEnterEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onLeaveItemClicked(final PlayerInteractEvent event) {
		if (event.getAction() == Action.PHYSICAL) return;

		final User user = plugin.getUserManager().getUser(event.getPlayer());
		final Arena arena = user.getArena();

		if (arena == null) return;
		if (event.getItem() == null) return;

		final GameItem leaveItem = plugin.getGameItemManager().getGameItem("leave-item");

		if (leaveItem == null) return;
		if (!event.getItem().getItemMeta().equals(leaveItem.getItemStack().getItemMeta())) return;

		final Player player = user.getPlayer();

		if (leaveConfirmations.contains(user)) {
			this.leaveConfirmations.remove(user);

			player.sendMessage(chatManager.message("in_game.game_items.leave_item.teleport_cancelled"));
		} else {
			player.sendMessage(chatManager.message("in_game.game_items.leave_item.returning_lobby"));

			this.leaveConfirmations.add(user);

			plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
				if (!this.leaveConfirmations.contains(user)) return;

				ArenaManager.leaveAttempt(player, arena);

				this.leaveConfirmations.remove(user);
			}, 60);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER && ArenaRegistry.isInArena((Player) event.getEntity())) {
			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onHangingBreakEvent(HangingBreakByEntityEvent event) {
		if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting) {
			if (event.getRemover() instanceof Player && ArenaRegistry.isInArena((Player) event.getRemover())) {
				event.setCancelled(true);
				return;
			}

			if (!(event.getRemover() instanceof Arrow)) {
				return;
			}

			Arrow arrow = (Arrow) event.getRemover();

			if (arrow.getShooter() instanceof Player && ArenaRegistry.isInArena((Player) arrow.getShooter())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onArmorStandDestroy(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}

		LivingEntity livingEntity = (LivingEntity) e.getEntity();

		if (!livingEntity.getType().equals(EntityType.ARMOR_STAND)) {
			return;
		}

		if (e.getDamager() instanceof Player && ArenaRegistry.isInArena((Player) e.getDamager())) {
			e.setCancelled(true);
		} else if (e.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) e.getDamager();

			if (arrow.getShooter() instanceof Player && ArenaRegistry.isInArena((Player) arrow.getShooter())) {
				e.setCancelled(true);
				return;
			}

			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInteractWithArmorStand(PlayerArmorStandManipulateEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemMove(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (ArenaRegistry.isInArena((Player) e.getWhoClicked())) {
				e.setResult(Event.Result.DENY);
			}
		}
	}

	@EventHandler
	public void playerCommandExecution(PlayerCommandPreprocessEvent e) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.ENABLE_SHORT_COMMANDS)) {
			Player player = e.getPlayer();

			if (e.getMessage().equalsIgnoreCase("/start")) {
				player.performCommand("tntrun forcestart");
				e.setCancelled(true);
				return;
			}

			if (e.getMessage().equalsIgnoreCase("/leave")) {
				player.performCommand("tntrun leave");
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}

		Player victim = (Player) e.getEntity();

		if (!ArenaRegistry.isInArena(victim)) {
			return;
		}

		if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onLobbyDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;

		Player player = (Player) event.getEntity();

		if (!ArenaRegistry.isInArena(player)) {
			return;
		}

		if (event.getDamage() < 500d && event.getCause() != EntityDamageEvent.DamageCause.VOID) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamageToEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) return;

		Player player = (Player) event.getEntity();

		if (!ArenaRegistry.isInArena(player)) return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
			return;
		}

		if (!(ArenaUtils.areInSameArena((Player) event.getDamager(), (Player) event.getEntity()))) {
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void onArrowPickup(PlayerPickupArrowEvent e) {
		if (ArenaRegistry.isInArena(e.getPlayer())) {
			e.getItem().remove();
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickupItem(PlayerPickupItemEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}

		event.setCancelled(true);
		event.getItem().remove();
	}

	@EventHandler
	public void onCraft(PlayerInteractEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}

		if (event.getPlayer().getTargetBlock(null, 7).getType() == XMaterial.CRAFTING_TABLE.parseMaterial()) {
			event.setCancelled(true);
		}
	}
}
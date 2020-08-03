package me.despical.tntrun.events;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaUtils;
import me.despical.tntrun.handlers.items.SpecialItemManager;
import me.despical.tntrun.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Events implements Listener {

	private final Main plugin;

	public Events(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onItemSwap(PlayerSwapHandItemsEvent e) {
		if (ArenaRegistry.isInArena(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onDrop(PlayerDropItemEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommandExecute(PlayerCommandPreprocessEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null) {
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
		if (event.getMessage().startsWith("/tntrun") || event.getMessage().startsWith("/tntrun") || event.getMessage().contains("leave") || event.getMessage().contains("stats")) {
			return;
		}
		event.setCancelled(true);
		event.getPlayer().sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Only-Command-Ingame-Is-Leave"));
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInGameInteract(PlayerInteractEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null || event.getClickedBlock() == null) {
			return;
		}
		if (event.getClickedBlock().getType() == XMaterial.PAINTING.parseMaterial() || event.getClickedBlock().getType() == XMaterial.FLOWER_POT.parseMaterial()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInGameBedEnter(PlayerBedEnterEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onLeave(PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
			return;
		}
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
		if (arena == null || !Utils.isNamed(itemStack)) {
			return;
		}
		String key = SpecialItemManager.getRelatedSpecialItem(itemStack);
		if (key == null) {
			return;
		}
		if (SpecialItemManager.getRelatedSpecialItem(itemStack).equalsIgnoreCase("Leave")) {
			event.setCancelled(true);
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				plugin.getBungeeManager().connectToHub(event.getPlayer());
			} else {
				ArenaManager.leaveAttempt(event.getPlayer(), arena);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity().getType() == EntityType.PLAYER && ArenaRegistry.isInArena((Player) event.getEntity())) {
			event.setFoodLevel(20);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBuild(BlockPlaceEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGH)
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

	@EventHandler(priority = EventPriority.HIGH)
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

	@EventHandler(priority = EventPriority.HIGH)
	public void onInteractWithArmorStand(PlayerArmorStandManipulateEvent event) {
		if (ArenaRegistry.isInArena(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onItemMove(InventoryClickEvent e) {
		if (e.getWhoClicked() instanceof Player) {
			if (ArenaRegistry.getArena((Player) e.getWhoClicked()) != null) {
				e.setResult(Event.Result.DENY);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
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
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onFallDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		Player victim = (Player) e.getEntity();
		Arena arena = ArenaRegistry.getArena(victim);
		if (arena == null) {
			return;
		}
		if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player && event.getEntity() instanceof Player)) {
			return;
		}
		if (!(ArenaUtils.areInSameArena((Player) event.getDamager(), (Player) event.getEntity()))) {
			return;
		}
		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onArrowPickup(PlayerPickupArrowEvent e) {
		if (ArenaRegistry.isInArena(e.getPlayer())) {
			e.getItem().remove();
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPickupItem(PlayerPickupItemEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null) {
			return;
		}
		event.setCancelled(true);
		event.getItem().remove();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onCraft(PlayerInteractEvent event) {
		if (!ArenaRegistry.isInArena(event.getPlayer())) {
			return;
		}
		if (event.getPlayer().getTargetBlock(null, 7).getType() == XMaterial.CRAFTING_TABLE.parseMaterial()) {
			event.setCancelled(true);
		}
	}
}
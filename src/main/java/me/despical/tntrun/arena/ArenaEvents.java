package me.despical.tntrun.arena;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.items.SpecialItemManager;
import me.despical.tntrun.user.User;
import me.despical.tntrun.utils.Utils;

public class ArenaEvents implements Listener {
	
	private Main plugin;
	
	public ArenaEvents(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		Player player = event.getPlayer();
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
		if (plugin.getUserManager().getUser(player).getCooldown("double_jump") > 0) {
			return;
		}
		if (SpecialItemManager.getRelatedSpecialItem(itemStack).equalsIgnoreCase("Double-Jump")) {
			event.setCancelled(true);
			if (StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
				event.setCancelled(true);
				plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
				plugin.getUserManager().getUser(player).setCooldown("double_jump", plugin.getConfig().getInt("Double-Jump-Delay", 4));
				player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getEntity();
		if (!(ArenaRegistry.isInArena(player))) {
			return;
		}
		if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
			player.damage(1000.0);
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Arena arena = ArenaRegistry.getArena(event.getEntity());
		if (!(ArenaRegistry.isInArena(event.getEntity()))) {
			return;
		}
		event.setDeathMessage("");
		event.getDrops().clear();
		event.setDroppedExp(0);
		event.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0, false, false), false);
		Player player = event.getEntity();
		User user = plugin.getUserManager().getUser(player);
		user.setSpectator(true);
	    player.setCollidable(false);
	    player.setGameMode(GameMode.SURVIVAL);
		ArenaUtils.hidePlayer(player, arena);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.getInventory().clear();
		plugin.getChatManager().broadcastAction(arena, player, ChatManager.ActionType.DEATH);
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			player.spigot().respawn();
			player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Item-Name", player)).build());
			player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Item-Name", player)).build());
			player.getInventory().setItem(8, SpecialItemManager.getSpecialItem("Leave").getItemStack());
		}, 5);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		Arena arena = ArenaRegistry.getArena(player);
		if (arena == null) {
			return;
		}
		if (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.IN_GAME) {
			e.setRespawnLocation(arena.getLobbyLocation());
		} else if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
			e.setRespawnLocation(arena.getEndLocation());
		}
		if (arena.getPlayers().contains(player)) {
			User user = plugin.getUserManager().getUser(player);
			if (player.getLocation().getWorld() == arena.getLobbyLocation().getWorld()) {
				e.setRespawnLocation(player.getLocation());
			} else {
				e.setRespawnLocation(arena.getLobbyLocation());
			}
			player.setAllowFlight(true);
			player.setFlying(true);
			user.setSpectator(true);
			ArenaUtils.hidePlayer(player, arena);
			player.setCollidable(false);
			player.setGameMode(GameMode.SURVIVAL);
			player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			user.setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, 0);
		}
	}
}
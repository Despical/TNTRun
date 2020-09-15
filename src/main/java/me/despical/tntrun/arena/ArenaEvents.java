package me.despical.tntrun.arena;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.item.ItemUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.items.SpecialItemManager;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.user.User;

public class ArenaEvents implements Listener {
	
	private final Main plugin;
	
	public ArenaEvents(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onDoubleJump(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();

		if (!event.isFlying() && player.getGameMode() != GameMode.ADVENTURE) {
			return;
		}

		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			return;
		}

		if (plugin.getUserManager().getUser(player).getCooldown("double_jump") > 0) {
			return;
		}

		if (StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0) {
			player.setFlying(false);

			plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
			plugin.getUserManager().getUser(player).setCooldown("double_jump", plugin.getConfig().getInt("Double-Jump-Delay", 4));

			player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.PHYSICAL) {
			return;
		}

		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();

		if (arena == null || !ItemUtils.isItemStackNamed(itemStack)) {
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

			if (StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0 && arena.getArenaState() == ArenaState.IN_GAME) {
				event.setCancelled(true);
				plugin.getUserManager().getUser(player).addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
				plugin.getUserManager().getUser(player).setCooldown("double_jump", plugin.getConfig().getInt("Double-Jump-Delay", 4));
				player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
				player.setFlying(false);
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

		Arena arena = ArenaRegistry.getArena(player);

		if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
			player.teleport(arena.getLobbyLocation());

			User user = plugin.getUserManager().getUser(player);
			user.setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, 0);

			player.setHealth(20.0d);

			if (user.isSpectator()) return;

			user.addStat(StatsStorage.StatisticType.LOSES, 1);
			user.setSpectator(true);

			plugin.getRewardsFactory().performReward(player, Reward.RewardType.LOSE);

			if (arena.getPlayersLeft().size() == 1) {
				Player winner = arena.getPlayersLeft().get(0);

				plugin.getUserManager().getUser(winner).addStat(StatsStorage.StatisticType.WINS, 1);
				plugin.getRewardsFactory().performReward(winner, Reward.RewardType.WIN);

				for (Player all : arena.getPlayers()) {
					if (all == winner) {
						all.sendTitle(plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Win"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Win").replace("%winner%", winner.getName()), 5, 40, 5);
					} else {
						all.sendTitle(plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Lose").replace("%winner%", winner.getName()), 5, 40, 5);
					}
				}
			}

			player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0, false, false), false);
			player.sendTitle(plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Titles.Lose"), plugin.getChatManager().colorMessage("In-Game.Messages.Game-End-Messages.Subtitles.Lose").replace("%winner%", arena.getPlayersLeft().get(0).getName()), 5, 40, 5);
			player.setCollidable(false);
			player.setGameMode(GameMode.SURVIVAL);
			player.setAllowFlight(true);
			player.setFlying(true);
			player.getInventory().clear();

			ArenaUtils.hidePlayer(player, arena);

			plugin.getChatManager().broadcastAction(arena, player, ChatManager.ActionType.DEATH);

			player.getInventory().setItem(0, new ItemBuilder(XMaterial.COMPASS.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Item-Name", player)).build());
			player.getInventory().setItem(4, new ItemBuilder(XMaterial.COMPARATOR.parseItem()).name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Item-Name", player)).build());
			player.getInventory().setItem(8, SpecialItemManager.getSpecialItem("Leave").getItemStack());
		}
	}
}
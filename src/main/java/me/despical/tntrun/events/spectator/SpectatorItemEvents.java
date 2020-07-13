package me.despical.tntrun.events.spectator;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.utils.Utils;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpectatorItemEvents implements Listener {

	private Main plugin;
	private SpectatorSettingsMenu spectatorSettingsMenu;
	private boolean usesPaperSpigot = Bukkit.getServer().getVersion().contains("Paper");

	public SpectatorItemEvents(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		spectatorSettingsMenu = new SpectatorSettingsMenu(plugin, plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Inventory-Name"), plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Speed-Name"));
	}

	@EventHandler
	public void onSpectatorItemClick(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() != Action.PHYSICAL) {
			if (ArenaRegistry.getArena(e.getPlayer()) == null) {
				return;
			}
			ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
			if (stack == null || !stack.hasItemMeta() || !stack.getItemMeta().hasDisplayName()) {
				return;
			}
			if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Item-Name"))) {
				e.setCancelled(true);
				openSpectatorMenu(e.getPlayer().getWorld(), e.getPlayer());
			} else if (stack.getItemMeta().getDisplayName().equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Item-Name"))) {
				e.setCancelled(true);
				spectatorSettingsMenu.openSpectatorSettingsMenu(e.getPlayer());
			}
		}
	}

	private void openSpectatorMenu(World world, Player p) {
		Inventory inventory = plugin.getServer().createInventory(null, Utils.serializeInt(ArenaRegistry.getArena(p).getPlayers().size()),
			plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Menu-Name"));
		Set<Player> players = ArenaRegistry.getArena(p).getPlayers();
		for (Player player : world.getPlayers()) {
			if (players.contains(player) && !plugin.getUserManager().getUser(player).isSpectator()) {
				ItemStack skull;
				if (plugin.is1_12_R1()) {
					skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
				} else {
					skull = XMaterial.PLAYER_HEAD.parseItem();
				}
				SkullMeta meta = (SkullMeta) skull.getItemMeta();
				if (usesPaperSpigot && player.getPlayerProfile().hasTextures()) {
					meta.setPlayerProfile(player.getPlayerProfile());
				} else {
					meta.setOwningPlayer(player);
				}
				meta.setDisplayName(player.getName());
				String score = plugin.getChatManager().colorMessage("In-Game.Spectator.Target-Player-Score");
				score = StringUtils.replace(score, "%score%", String.valueOf(StatsStorage.getUserStats(player, StatsStorage.StatisticType.LOCAL_COINS)));
				skull.setLore(Collections.singletonList(score));
				skull.setDurability((short) SkullType.PLAYER.ordinal());
				skull.setItemMeta(meta);
				inventory.addItem(skull);
			}
		}
		p.openInventory(inventory);
	}

	@EventHandler
	public void onSpectatorInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (ArenaRegistry.getArena(p) == null) {
			return;
		}
		Arena arena = ArenaRegistry.getArena(p);
		if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta() || !e.getCurrentItem().getItemMeta().hasDisplayName() || !e.getCurrentItem().getItemMeta().hasLore()) {
			return;
		}
		if (!e.getView().getTitle().equalsIgnoreCase(plugin.getChatManager().colorMessage("In-Game.Spectator.Spectator-Menu-Name", p))) {
			return;
		}
		e.setCancelled(true);
		ItemMeta meta = e.getCurrentItem().getItemMeta();
		for (Player player : arena.getPlayers()) {
			if (player.getName().equalsIgnoreCase(meta.getDisplayName()) || ChatColor.stripColor(meta.getDisplayName()).contains(player.getName())) {
				p.sendMessage(plugin.getChatManager().formatMessage(arena, plugin.getChatManager().colorMessage("Commands.Admin-Commands.Teleported-To-Player"), player));
				p.teleport(player);
				p.closeInventory();
				return;
			}
		}
		p.sendMessage(plugin.getChatManager().colorMessage("Commands.Admin-Commands.Player-Not-Found"));
	}
}
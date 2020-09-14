package me.despical.tntrun.handlers.setup.components;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpawnComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		Player player = setupInventory.getPlayer();
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();
		String serializedLocation = LocationSerializer.locationToString(player.getLocation());

		pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE_BLOCK)
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Ending Location"))
			.lore(ChatColor.GRAY + "Click to set the ending location")
			.lore(ChatColor.GRAY + "on the place where you are standing.")
			.lore(ChatColor.DARK_GRAY + "(location where players will be")
			.lore(ChatColor.DARK_GRAY + "teleported after the game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool("instances." + arena.getId() + ".Endlocation"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				config.set("instances." + arena.getId() + ".Endlocation", serializedLocation);
				arena.setEndLocation(player.getLocation());
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));
				ConfigUtils.saveConfig(plugin, config, "arenas");
			}), 0, 0);

		pane.addItem(new GuiItem(new ItemBuilder(Material.LAPIS_BLOCK)
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Lobby Location"))
			.lore(ChatColor.GRAY + "Click to set the lobby location")
			.lore(ChatColor.GRAY + "on the place where you are standing")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool("instances." + arena.getId() + ".lobbylocation"))
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				config.set("instances." + arena.getId() + ".lobbylocation", serializedLocation);
				arena.setLobbyLocation(player.getLocation());
				player.sendMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aLobby location for arena " + arena.getId() + " set at your location!"));
				ConfigUtils.saveConfig(plugin, config, "arenas");
			}), 1, 0);
	}
}
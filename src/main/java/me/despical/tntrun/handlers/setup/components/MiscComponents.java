package me.despical.tntrun.handlers.setup.components;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;

import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;
import me.despical.tntrun.handlers.sign.ArenaSign;
import me.despical.tntrun.utils.conversation.SimpleConversationBuilder;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class MiscComponents implements SetupComponent {

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
		ItemStack bungeeItem;

		if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			bungeeItem = new ItemBuilder(XMaterial.OAK_SIGN.parseMaterial())
				.name(plugin.getChatManager().colorRawMessage("&e&lAdd Game Sign"))
				.lore(ChatColor.GRAY + "Target a sign and click this.")
				.lore(ChatColor.DARK_GRAY + "(this will set target sign as game sign)").build();
		} else {
			bungeeItem = new ItemBuilder(Material.BARRIER)
				.name(plugin.getChatManager().colorRawMessage("&c&lAdd Game Sign"))
				.lore(ChatColor.GRAY + "Option disabled in bungee cord mode.")
				.lore(ChatColor.DARK_GRAY + "Bungee mode is meant to be one arena per server")
				.lore(ChatColor.DARK_GRAY + "If you wish to have multi arena, disable bungee in config!")
				.build();
		}

		pane.addItem(new GuiItem(bungeeItem, e -> {
			if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
				return;
			}

			e.getWhoClicked().closeInventory();

			Location location = player.getTargetBlock(null, 10).getLocation();

			if (!(location.getBlock().getState() instanceof Sign)) {
				player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Look-Sign"));
				return;
			}

			if (location.distance(e.getWhoClicked().getWorld().getSpawnLocation()) <= Bukkit.getServer().getSpawnRadius() && e.getClick() != ClickType.SHIFT_LEFT) {
				e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Server spawn protection is set to &6" + Bukkit.getServer().getSpawnRadius() + " &cand sign you want to place is in radius of this protection! &c&lNon opped players won't be able to interact with this sign and can't join the game so."));
				e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&cYou can ignore this warning and add sign with Shift + Left Click, but for now &c&loperation is cancelled"));
				return;
			}

			plugin.getSignManager().getArenaSigns().add(new ArenaSign((Sign) location.getBlock().getState(), arena));
			player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Signs.Sign-Created"));

			String signLoc = LocationSerializer.locationToString(location);
			List<String> locs = config.getStringList("instances." + arena.getId() + ".signs");

			locs.add(signLoc);
			config.set("instances." + arena.getId() + ".signs", locs);
			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 4, 0);

		pane.addItem(new GuiItem(new ItemBuilder(Material.NAME_TAG)
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Map Name"))
			.lore(ChatColor.GRAY + "Click to set arena map name")
			.lore("", plugin.getChatManager().colorRawMessage("&a&lCurrently: &e" + config.getString("instances." + arena.getId() + ".mapname")))
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				new SimpleConversationBuilder().withPrompt(new StringPrompt() {

					@Override
					public String getPromptText(ConversationContext context) {
						return plugin.getChatManager().colorRawMessage(plugin.getChatManager().getPrefix() + "&ePlease type in chat arena name! You can use color codes.");
					}

					@Override
					public Prompt acceptInput(ConversationContext context, String input) {
						String name = plugin.getChatManager().colorRawMessage(input);

						player.sendRawMessage(plugin.getChatManager().colorRawMessage("&e✔ Completed | &aName of arena " + arena.getId() + " set to " + name));
						arena.setMapName(name);
						config.set("instances." + arena.getId() + ".mapname", arena.getMapName());
						ConfigUtils.saveConfig(plugin, config, "arenas");

						new SetupInventory(arena, player).openInventory();
						return Prompt.END_OF_CONVERSATION;
					}
				}).buildFor(player);
			}), 5, 0);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP.parseItem())
			.name(plugin.getChatManager().colorRawMessage("&e&lView Wiki Page"))
			.lore(ChatColor.GRAY + "Having problems with setup or wanna")
			.lore(ChatColor.GRAY + "know some useful tips? Click to get wiki link!")
			.build(), e -> {
				e.getWhoClicked().closeInventory();
				player.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorRawMessage("&7Check out our wiki: https://github.com/Despical/TNTRun/wiki"));
			}), 7, 0);
	}
}

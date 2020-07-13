package me.despical.tntrun.handlers.setup.components;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PlayerAmountComponents implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		FileConfiguration config = setupInventory.getConfig();
		Arena arena = setupInventory.getArena();
		Main plugin = setupInventory.getPlugin();
		pane.addItem(new GuiItem(new ItemBuilder(Material.COAL)
			.amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("minimumplayers"))
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Minimum Players Amount"))
			.lore(ChatColor.GRAY + "LEFT click to decrease").lore(ChatColor.GRAY + "RIGHT click to increase")
			.lore(ChatColor.DARK_GRAY + "(how many players are needed")
			.lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)").lore("", setupInventory
			.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".minimumplayers"))
			.build(), e -> {
				if (e.getClick().isRightClick()) {
					e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() + 1);
				}
				if (e.getClick().isLeftClick()) {
					e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() - 1);
				}
				if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
					e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
					e.getInventory().getItem(e.getSlot()).setAmount(2);
				}
				config.set("instances." + arena.getId() + ".minimumplayers", e.getCurrentItem().getAmount());
				arena.setMinimumPlayers(e.getCurrentItem().getAmount());
				ConfigUtils.saveConfig(plugin, config, "arenas");
				new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
			}), 2, 0);

		pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE)
			.amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("maximumplayers"))
			.name(plugin.getChatManager().colorRawMessage("&e&lSet Maximum Players Amount"))
			.lore(ChatColor.GRAY + "LEFT click to decrease").lore(ChatColor.GRAY + "RIGHT click to increase")
			.lore(ChatColor.DARK_GRAY + "(how many players arena can hold)").lore("", setupInventory
			.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".maximumplayers"))
			.build(), e -> {
				if (e.getClick().isRightClick()) {
					e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
				}
				if (e.getClick().isLeftClick()) {
					e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
				}
				if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
					e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
					e.getInventory().getItem(e.getSlot()).setAmount(2);
				}
				config.set("instances." + arena.getId() + ".maximumplayers", e.getCurrentItem().getAmount());
				arena.setMaximumPlayers(e.getCurrentItem().getAmount());
				ConfigUtils.saveConfig(plugin, config, "arenas");
				new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
			}), 3, 0);
	}
}
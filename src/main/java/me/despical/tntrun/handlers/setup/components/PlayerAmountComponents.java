/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

package me.despical.tntrun.handlers.setup.components;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

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
			.name("&e&lSet Minimum Players Amount")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(how many players are needed")
			.lore("&8for game to start lobby countdown)").lore("", setupInventory
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
			.name("&e&lSet Maximum Players Amount")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(how many players arena can hold)")
			.lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".maximumplayers"))
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
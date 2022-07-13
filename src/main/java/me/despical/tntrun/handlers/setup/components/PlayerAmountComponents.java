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

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PlayerAmountComponents implements SetupComponent {

	@Override
	public void registerComponent(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		Arena arena = setupInventory.getArena();
		String error = chatManager.color("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!");

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.COAL)
			.amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("minimumPlayers"))
			.name("&e&lSet Minimum Players Amount")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(how many players are needed")
			.lore("&8for game to start lobby countdown)")
			.lore("", setupInventory.getSetupUtilities().isOptionDone("minimumPlayers"))
			.build(), e -> {

			int amount = e.getCurrentItem().getAmount();

			if (e.getClick().isRightClick()) {
				e.getCurrentItem().setAmount(++amount);
			}

			if (e.getClick().isLeftClick()) {
				e.getCurrentItem().setAmount(--amount);
			}

			if (amount <= 1) {
				player.sendMessage(error);
				amount++;
			}

			arena.setMinimumPlayers(amount);

			config.set("instances." + arena.getId() + ".minimumPlayers", amount);
			saveConfig();

			new SetupInventory(plugin, arena, player).openInventory();
		}), 3, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE)
			.amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("maximumPlayers"))
			.name("&e&lSet Maximum Players Amount")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(how many players arena can hold)")
			.lore("", setupInventory.getSetupUtilities().isOptionDone("maximumPlayers"))
			.build(), e -> {

			int amount = e.getCurrentItem().getAmount();

			if (e.getClick().isRightClick()) {
				e.getCurrentItem().setAmount(++amount);
			}

			if (e.getClick().isLeftClick()) {
				e.getCurrentItem().setAmount(--amount);
			}

			if (amount <= 1) {
				player.sendMessage(error);
				amount++;
			}

			arena.setMaximumPlayers(amount);

			config.set("instances." + arena.getId() + ".maximumPlayers", amount);
			saveConfig();

			new SetupInventory(plugin, arena, player).openInventory();
		}), 4, 1);
	}
}
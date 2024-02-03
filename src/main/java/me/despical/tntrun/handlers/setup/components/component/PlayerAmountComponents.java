/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.tntrun.handlers.setup.components.component;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import me.despical.tntrun.handlers.setup.components.AbstractComponent;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 * Recoded at 21.05.2023
 */
public class PlayerAmountComponents extends AbstractComponent {

	public PlayerAmountComponents(ArenaEditorGUI gui) {
		super(gui);
	}

	@Override
	public void registerComponents(PaginatedPane paginatedPane) {
		final var pane = new StaticPane(9, 3);
		final var backgroundDone = isOptionDoneBoolean("lobbyLocation") && isOptionDoneBoolean("endLocation");

		final var backgroundItem = backgroundDone ?
			new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE).name("&aGame locations set properly!") :
			new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cSet game locations properly!");

		final var minPlayersItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
			.name("&e&lSet Minimum Players")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(how many players are needed")
			.lore("&8for game to start lobby countdown)")
			.lore("", isOptionDone("minimumPlayers"))
			.amount(minValueHigherThan("minimumPlayers", 2));

		final var maxPlayersItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
			.name("&e&lSet Maximum Players")
			.lore("&7LEFT click to decrease")
			.lore("&7RIGHT click to increase")
			.lore("&8(player amount that arena")
			.lore("&8can hold)")
			.lore("", isOptionDone("maximumPlayers"))
			.amount(minValueHigherThan("maximumPlayers", arena.getMinimumPlayers()));

		pane.fillWith(backgroundItem.build(), event -> event.setCancelled(true));
		pane.addItem(GuiItem.of(mainMenuItem, event -> this.gui.restorePage()), 8, 2);

		pane.addItem(GuiItem.of(minPlayersItem.build(), event -> {
			var amount = event.getCurrentItem().getAmount();

			if (event.getClick().isRightClick()) {
				event.getCurrentItem().setAmount(++amount);
			}

			if (event.getClick().isLeftClick()) {
				event.getCurrentItem().setAmount(--amount);
			}

			if (amount < 2) {
				user.sendRawMessage("&c&l✘ Minimum players amount cannot be less than 2!");

				amount = 2;
			}

			if (amount > arena.getMaximumPlayers()) {
				user.sendRawMessage("&c&l✘ Minimum players amount cannot be higher than maximum players amount! Setting the as the same value!");

				arena.setMaximumPlayers(amount);

				config.set(path + "maximumPlayers", amount);
			}

			arena.setMinimumPlayers(amount);
			arena.updateSigns();

			config.set(path + "minimumPlayers", amount);
			saveConfig();

			gui.reshowGuiFromCurrentPage();
		}), 3, 1);

		pane.addItem(GuiItem.of(maxPlayersItem.build(), event -> {
			var item = event.getCurrentItem();
			var amount = item.getAmount();
			var click = event.getClick();

			if (click.isRightClick()) {
				item.setAmount(++amount);
			}

			if (click.isLeftClick()) {
				item.setAmount(--amount);
			}

			if (amount < arena.getMinimumPlayers()) {
				user.sendRawMessage("&c&l✘ Maximum players amount cannot be less than 2!");

				amount = arena.getMinimumPlayers();
			}

			arena.setMaximumPlayers(amount);
			arena.updateSigns();

			config.set(path + "maximumPlayers", amount);
			saveConfig();

			gui.reshowGuiFromCurrentPage();
		}), 5, 1);

		paginatedPane.addPane(3, pane);
	}
}
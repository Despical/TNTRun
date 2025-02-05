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

import me.despical.commons.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
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
		var pane = new StaticPane(9, 3);
		var config = ConfigUtils.getConfig(plugin, "arena");
		var backgroundItem = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE).name("&aSet min/max player amounts!");

		var minPlayersItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
				.name("&e&l       Set Minimum Players")
				.lore("&8• &7LEFT  click to increase")
				.lore("&8• &7RIGHT click to decrease", "")
				.lore("&8• &7How many players are needed for")
				.lore("&7game to start the lobby countdown.")
				.lore("", isOptionDone("minimumPlayers", config))
				.amount(minValueHigherThan("minimumPlayers", 2, config));

		var maxPlayersItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST)
				.name("&e&l      Set Maximum Players")
				.lore("&8• &7LEFT  click to increase")
				.lore("&8• &7RIGHT click to decrease", "")
				.lore("&8• &7Maximum player amount that arena", "&7can hold.")
				.lore("", isOptionDone("maximumPlayers", config))
				.amount(minValueHigherThan("maximumPlayers", arena.getMinimumPlayers(), config));

		pane.fillWith(backgroundItem.build(), event -> event.setCancelled(true));
		pane.addItem(GuiItem.of(mainMenuItem, event -> this.gui.restorePage()), 8, 2);

		pane.addItem(GuiItem.of(minPlayersItem.build(), event -> {
			var item = event.getCurrentItem();
			var amount = item.getAmount();
			var click = event.getClick();

			item.setAmount(click.isRightClick() ? --amount : click.isLeftClick() ? ++amount : amount);

			if (event.getCurrentItem().getAmount() < 2) {
				user.sendRawMessage("&c&l✘ Minimum players amount cannot be less than 2!");

				amount = 2;
				item.setAmount(2);
			}

			if (item.getAmount() > arena.getMaximumPlayers()) {
				user.sendRawMessage("&c&l✘ Minimum player amount cannot be higher than maximum players amount! Setting both as the same value!");

				arena.setMaximumPlayers(amount);

				config.set(path + "maximumPlayers", amount);

				item.setAmount(amount);
			}

			arena.setMinimumPlayers(amount);
			arena.updateSigns();

			config.set(path + "minimumPlayers", amount);
			ConfigUtils.saveConfig(plugin, config, "arena");

			gui.reshowGuiFromCurrentPage();
		}), 3, 1);

		pane.addItem(GuiItem.of(maxPlayersItem.build(), event -> {
			var item = event.getCurrentItem();
			var amount = item.getAmount();
			var click = event.getClick();

			item.setAmount(click.isRightClick() ? --amount : click.isLeftClick() ? ++amount : amount);

			if (item.getAmount() < 2) {
				user.sendRawMessage("&c&l✘ Maximum player amount cannot be less than 2!");

				amount = 2;
				item.setAmount(2);
			} else if (item.getAmount() < arena.getMinimumPlayers()) {
				user.sendRawMessage("&c&l✘ Maximum player amount cannot be less than minimum player amount! Setting both as the same value!");

				arena.setMinimumPlayers(amount);

				config.set(path + "minimumPlayers", amount);

				item.setAmount(amount);
			}

			arena.setMaximumPlayers(amount);
			arena.updateSigns();

			config.set(path + "maximumPlayers", amount);
			ConfigUtils.saveConfig(plugin, config, "arena");

			gui.reshowGuiFromCurrentPage();
		}), 5, 1);

		paginatedPane.addPane(3, pane);
	}
}

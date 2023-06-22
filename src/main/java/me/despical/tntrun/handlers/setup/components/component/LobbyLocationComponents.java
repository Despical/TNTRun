/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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
import me.despical.commons.serializer.LocationSerializer;
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
public class LobbyLocationComponents extends AbstractComponent {

	public LobbyLocationComponents(ArenaEditorGUI gui) {
		super(gui);
	}

	@Override
	public void registerComponents(PaginatedPane paginatedPane) {
		final var pane = new StaticPane(9, 3);
		final var backgroundDone = isOptionDoneBoolean("lobbyLocation") && isOptionDoneBoolean("endLocation");

		final var backgroundItem = backgroundDone ?
			new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE).name("&aGame locations set properly!") :
			new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cSet game locations properly!");

		final var endLocationItem = new ItemBuilder(XMaterial.ORANGE_CONCRETE)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set the ending location")
			.lore("&7on place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the game ended)")
			.lore("", isOptionDoneBool("endLocation"));

		final var lobbyLocationItem = new ItemBuilder(XMaterial.CYAN_CONCRETE)
			.name("&e&lSet Lobby Location")
			.lore("&7Click to set lobby location")
			.lore("&7on place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported before the game starts)")
			.lore("", isOptionDoneBool("lobbyLocation"));

		pane.fillWith(backgroundItem.build(), event -> event.setCancelled(true));
		pane.addItem(GuiItem.of(mainMenuItem, event -> this.gui.restorePage()), 8, 2);

		pane.addItem(GuiItem.of(lobbyLocationItem.build(), event -> {
			final var location = user.getLocation();

			if (!event.isShiftClick()) user.closeOpenedInventory();

			config.set(path + "lobbyLocation", LocationSerializer.toString(location));
			saveConfig();

			arena.setLobbyLocation(location);

			user.sendRawMessage("&e✔ Completed | &aLobby location for arena &e%s &aset at your location!", arena);
		}), 3, 1);

		pane.addItem(GuiItem.of(endLocationItem.build(), event -> {
			final var location = user.getLocation();

			if (!event.isShiftClick()) user.closeOpenedInventory();

			config.set(path + "endLocation", LocationSerializer.toString(location));
			saveConfig();

			arena.setEndLocation(location);

			user.sendRawMessage("&e✔ Completed | &aEnding location for arena &e%s &aset at your location!", arena);
		}), 5, 1);

		paginatedPane.addPane(1, pane);
	}
}
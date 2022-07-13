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
import me.despical.commons.serializer.LocationSerializer;
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
public class SpawnComponents implements SetupComponent {

	@Override
	public void registerComponent(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		Arena arena = setupInventory.getArena();
		String serializedLocation = LocationSerializer.toString(player.getLocation());

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.REDSTONE_BLOCK)
			.name("&e&lSet Ending Location")
			.lore("&7Click to set the ending location")
			.lore("&7on the place where you are standing.")
			.lore("&8(location where players will be")
			.lore("&8teleported after the game)")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool("endLocation"))
			.build(), e -> {

			arena.setEndLocation(player.getLocation());

			player.closeInventory();
			player.sendMessage(chatManager.color("&e✔ Completed | &aEnding location for arena " + arena.getId() + " set at your location!"));

			config.set("instances." + arena.getId() + ".endLocation", serializedLocation);
			saveConfig();
		}), 1, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.LAPIS_BLOCK)
			.name(chatManager.color("&e&lSet Lobby Location"))
			.lore("&7Click to set the lobby location")
			.lore("&7on the place where you are standing")
			.lore("", setupInventory.getSetupUtilities().isOptionDoneBool("lobbyLocation"))
			.build(), e -> {

			arena.setLobbyLocation(player.getLocation());

			player.closeInventory();
			player.sendMessage(chatManager.color("&e✔ Completed | &aLobby location for arena " + arena.getId() + " set at your location!"));

			config.set("instances." + arena.getId() + ".lobbyLocation", serializedLocation);
			saveConfig();
		}), 2, 1);
	}
}
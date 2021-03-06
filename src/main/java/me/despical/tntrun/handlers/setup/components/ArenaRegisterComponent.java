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
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.setup.SetupInventory;
import me.despical.tntrun.handlers.sign.ArenaSign;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaRegisterComponent implements SetupComponent {

	private SetupInventory setupInventory;

	@Override
	public void prepare(SetupInventory setupInventory) {
		this.setupInventory = setupInventory;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		FileConfiguration config = setupInventory.getConfig();
		Main plugin = setupInventory.getPlugin();
		ItemStack registeredItem;

		if (!setupInventory.getArena().isReady()) {
			registeredItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET.parseItem())
				.name("&e&lRegister Arena - Finish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register arena.")
				.build();
		} else {
			registeredItem = new ItemBuilder(Material.BARRIER)
				.name(plugin.getChatManager().colorRawMessage("&a&lArena Registered - Congratulations"))
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.lore("&7You can play on this arena now!")
				.enchantment(Enchantment.DURABILITY)
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		}

		pane.addItem(new GuiItem(registeredItem, e -> {
			Arena arena = setupInventory.getArena();
			e.getWhoClicked().closeInventory();

			if (ArenaRegistry.getArena(setupInventory.getArena().getId()).isReady()) {
				e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!"));
				return;
			}

			String[] locations = {"lobbylocation", "Endlocation"};

			for (String s : locations) {
				if (!config.isSet("instances." + arena.getId() + "." + s) || config.getString("instances." + arena.getId() + "." + s).equals(LocationSerializer.toString(Bukkit.getWorlds().get(0).getSpawnLocation()))) {
					e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: " + s + " (cannot be world spawn location)"));
					return;
				}
			}

			e.getWhoClicked().sendMessage(plugin.getChatManager().colorRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: " + arena.getId()));
			config.set("instances." + arena.getId() + ".isdone", true);
			ConfigUtils.saveConfig(plugin, config, "arenas");

			List<Sign> signsToUpdate = new ArrayList<>();

			ArenaRegistry.unregisterArena(setupInventory.getArena());

			for (ArenaSign arenaSign : plugin.getSignManager().getArenaSigns()) {
				if (arenaSign.getArena().equals(setupInventory.getArena())) {
					signsToUpdate.add(arenaSign.getSign());
				}
			}

			arena.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
			arena.setReady(true);
			arena.setMinimumPlayers(config.getInt("instances." + arena.getId() + ".minimumplayers"));
			arena.setMaximumPlayers(config.getInt("instances." + arena.getId() + ".maximumplayers"));
			arena.setMapName(config.getString("instances." + arena.getId() + ".mapname"));
			arena.setLobbyLocation(LocationSerializer.fromString(config.getString("instances." + arena.getId() + ".lobbylocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString("instances." + arena.getId() + ".Endlocation")));
			ArenaRegistry.registerArena(arena);
			arena.start();

			signsToUpdate.forEach(s -> plugin.getSignManager().getArenaSigns().add(new ArenaSign(s, arena)));

			ConfigUtils.saveConfig(plugin, config, "arenas");
		}), 8, 0);
	}
}
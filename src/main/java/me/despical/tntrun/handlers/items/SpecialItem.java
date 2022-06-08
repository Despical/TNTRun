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

package me.despical.tntrun.handlers.items;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.util.Collections;
import me.despical.tntrun.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.stream.Collectors;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpecialItem {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private final int slot;
	private final ItemStack itemStack;

	public SpecialItem(String key, String displayName, int slot, XMaterial material, String... lore) {
		this.slot = slot;

		FileConfiguration config = ConfigUtils.getConfig(plugin, "lobbyitems");

		if (!config.contains(key)) {
			config.set(key + ".displayName", displayName);
			config.set(key + ".slot", slot);
			config.set(key + ".materialName", material);
			config.set(key + ".lore", Collections.listOf(lore));

			ConfigUtils.saveConfig(plugin, config, "items");
		}


		final ItemStack itemStack = XMaterial.matchXMaterial(material.toString()).orElse(XMaterial.STONE).parseItem();
		final ItemMeta meta = itemStack.getItemMeta();
		meta.setDisplayName(plugin.getChatManager().color(displayName));
		meta.setLore(Collections.listOf(lore).stream().map(plugin.getChatManager()::color).collect(Collectors.toList()));

		itemStack.setItemMeta(meta);

		this.itemStack = itemStack;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public int getSlot() {
		return slot;
	}
}
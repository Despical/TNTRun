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
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpecialItemManager {

	private final Map<String, SpecialItem> specialItems;

	public SpecialItemManager() {
		this.specialItems = new HashMap<>();

		this.addItem("Leave", new SpecialItem("Leave", "&cLeave", 8, XMaterial.WHITE_BED, "&7Click to teleport to hub"));
		this.addItem("Double-Jump", new SpecialItem("Double-Jump", "&cDouble-Jump", 0, XMaterial.FEATHER, "&7Click to double jump"));
	}

	public void addItem(String name, SpecialItem specialItem) {
		specialItems.put(name, specialItem);
	}

	public SpecialItem getSpecialItem(String name) {
		return specialItems.get(name);
	}

	public String getRelatedSpecialItem(ItemStack itemStack) {
		for (String key : specialItems.keySet()) {
			SpecialItem specialItem = specialItems.get(key);

			if (specialItem.itemStack.equals(itemStack)) {
				return key;
			}
		}

		return null;
	}
}
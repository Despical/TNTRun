package me.despical.tntrun.handlers.items;

import java.util.HashMap;

import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpecialItemManager {

	private static HashMap<String, SpecialItem> specialItems = new HashMap<>();

	public static void addItem(String name, SpecialItem entityItem) {
		specialItems.put(name, entityItem);
	}

	public static SpecialItem getSpecialItem(String name) {
		if (specialItems.containsKey(name)) {
			return specialItems.get(name);
		}
		return null;
	}

	public static String getRelatedSpecialItem(ItemStack itemStack) {
		for (String key : specialItems.keySet()) {
			SpecialItem entityItem = specialItems.get(key);
			if (entityItem.getItemStack().getItemMeta().getDisplayName().equalsIgnoreCase(itemStack.getItemMeta().getDisplayName())) {
				return key;
			}
		}
		return null;
	}
}
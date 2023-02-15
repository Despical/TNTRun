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

package me.despical.tntrun.handlers.items;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class GameItemManager {

	private final Main plugin;
	private final Map<String, GameItem> gameItems;

	public GameItemManager(final Main plugin) {
		this.plugin = plugin;
		this.gameItems = new HashMap<>();
		this.registerItems();
	}

	public GameItem getGameItem(final String id) {
		return this.gameItems.get(id);
	}

	private void registerItems() {
		final FileConfiguration config = ConfigUtils.getConfig(plugin, "items");
		final ConfigurationSection section = config.getConfigurationSection("items");

		if (section == null) {
			plugin.getLogger().log(Level.WARNING, "Couldn't find 'items' section in items.yml, delete the file to regenerate it!");
			return;
		}

		for (final String id : section.getKeys(false)) {
			final String path = String.format("items.%s.", id);
			final GameItem gameItem = new GameItem(config.getString(path + "name"), XMaterial.valueOf(config.getString(path + "material")).parseMaterial(), config.getInt(path + "slot"), config.getStringList(path + "lore"));

			this.gameItems.put(id, gameItem);
		}
	}
}
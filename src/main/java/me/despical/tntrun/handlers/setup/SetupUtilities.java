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

package me.despical.tntrun.handlers.setup;

import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SetupUtilities {

	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final FileConfiguration config;
	private final Arena arena;

	SetupUtilities(FileConfiguration config, Arena arena) {
		this.config = config;
		this.arena = arena;
	}

	public String isOptionDone(String path) {
		if (config.isSet(path)) {
			return plugin.getChatManager().colorRawMessage("&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)");
		}

		return plugin.getChatManager().colorRawMessage("&c&l✘ Not Completed");
	}

	public String isOptionDoneBool(String path) {
		if (config.isSet(path)) {
			if (Bukkit.getServer().getWorlds().get(0).getSpawnLocation().equals(LocationSerializer.locationFromString(config.getString(path)))) {
				return plugin.getChatManager().colorRawMessage("&c&l✘ Not Completed");
			}

			return plugin.getChatManager().colorRawMessage("&a&l✔ Completed");
		}

		return plugin.getChatManager().colorRawMessage("&c&l✘ Not Completed");
	}

	public int getMinimumValueHigherThanZero(String path) {
		int amount = config.getInt("instances." + arena.getId() + "." + path);

		return amount == 0 ? 1 : amount;
	}
}
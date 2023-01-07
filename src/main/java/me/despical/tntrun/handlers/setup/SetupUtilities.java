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

package me.despical.tntrun.handlers.setup;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.tntrun.Main;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SetupUtilities {

	private final String id;
	private final FileConfiguration config;

	SetupUtilities(Main plugin, String id) {
		this.id = id;
		this.config = ConfigUtils.getConfig(plugin, "arenas");
	}

	public String isOptionDone(String path) {
		path = String.format("instances.%s.%s", id, path);
		return config.isSet(path) ? "&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)" : "&c&l✘ Not Completed";
	}

	public String isOptionDoneBool(String path) {
		path = String.format("instances.%s.%s", id, path);
		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}

	public int getMinimumValueHigherThanZero(String path) {
		int amount = config.getInt(String.format("instances.%s.%s", id, path));

		return amount == 0 ? 1 : amount;
	}
}
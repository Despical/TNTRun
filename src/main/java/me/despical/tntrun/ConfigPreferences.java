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

package me.despical.tntrun;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ConfigPreferences {

	private final Main plugin;
	private final boolean papi;
	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;
		this.papi = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
		this.options = new HashMap<>();

		loadOptions();
	}

	public boolean isPapiEnabled() {
		return papi;
	}

	/**
	 * Returns whether option value is true or false
	 *
	 * @param option option to get value from
	 * @return true or false based on user configuration
	 */
	public boolean getOption(Option option) {
		return options.get(option);
	}

	private void loadOptions() {
		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault()));
		}
	}

	public enum Option {
		BOSS_BAR_ENABLED("Boss-Bar-Enabled", true), BUNGEE_ENABLED("Bungee-Activated", false),
		CHAT_FORMAT_ENABLED("Chat-Format-Enabled", true), DATABASE_ENABLED("Database-Activated", false),
		DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false), ENABLE_SHORT_COMMANDS("Enable-Short-Commands", false),
		INVENTORY_MANAGER_ENABLED("Inventory-Manager", true), NAME_TAGS_HIDDEN("Name-Tags-Hidden", false);

		private final String path;
		private final boolean def;

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		public String getPath() {
			return path;
		}

		public boolean getDefault() {
			return def;
		}
	}
}
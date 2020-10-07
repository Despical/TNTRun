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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ConfigPreferences {

	private final Main plugin;
	private final Map<Option, Boolean> options = new HashMap<>();

	public ConfigPreferences(Main plugin) {
		this.plugin = plugin;

		loadOptions();
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
		Arrays.stream(Option.values()).forEach(option -> options.put(option, plugin.getConfig().getBoolean(option.getPath(), option.getDefault())));
	}

	public enum Option {
		BOSSBAR_ENABLED("Bossbar-Enabled", true), BUNGEE_ENABLED("BungeeActivated", false),
		CHAT_FORMAT_ENABLED("ChatFormat-Enabled", true), DATABASE_ENABLED("DatabaseActivated", false),
		DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false), ENABLE_SHORT_COMMANDS("Enable-Short-Commands", false),
		INVENTORY_MANAGER_ENABLED("InventoryManager", true), NAMETAGS_HIDDEN("Nametags-Hidden", false);

		private final String path;
		private final boolean def;

		Option(String path, boolean def) {
			this.path = path;
			this.def = def;
		}

		public String getPath() {
			return path;
		}

		/**
		 * @return default value of option if absent in config
		 */
		public boolean getDefault() {
			return def;
		}
	}
}
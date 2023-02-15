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

package me.despical.tntrun;

import me.despical.commons.string.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ConfigPreferences {

	private final Map<Option, Boolean> options;

	public ConfigPreferences(Main plugin) {
		this.options = new HashMap<>();

		plugin.saveDefaultConfig();

		for (Option option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public enum Option {

		BOSS_BAR_ENABLED(true), CHAT_FORMAT_ENABLED(true), DATABASE_ENABLED,
		DEBUG_MESSAGES, DISABLE_LEAVE_COMMAND, DISABLE_SEPARATE_CHAT,
		ENABLE_SHORT_COMMANDS, INVENTORY_MANAGER_ENABLED(true),
		NAME_TAGS_HIDDEN, REWARDS_ENABLED, SIGNS_BLOCK_STATES_ENABLED(true), UPDATE_NOTIFIER_ENABLED;

		String path;
		boolean def;

		Option() {
			this(false);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(), '_');
		}
	}
}
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

import me.despical.commons.serializer.InventorySerializer;
import me.despical.commons.string.StringUtils;
import me.despical.commons.util.function.DoubleSupplier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Locale;
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

		for (final var option : Option.values()) {
			options.put(option, plugin.getConfig().getBoolean(option.path, option.def));
		}
	}

	public boolean getOption(Option option) {
		return options.get(option);
	}

	public enum Option {

		BUNGEE_ENABLED(false), GAME_BAR_ENABLED, INVENTORY_MANAGER_ENABLED("Inventory-Manager.Enabled"), DISABLE_FALL_DAMAGE, DATABASE_ENABLED(false),
		JUMP_BAR, NAME_TAGS_HIDDEN(false), CHAT_FORMAT_ENABLED, DISABLE_SEPARATE_CHAT(false), UPDATE_NOTIFIER_ENABLED, INSTANT_LEAVE(false),

		HEAL_PLAYER((config) -> {
			final var list = config.getStringList("Inventory-Manager.Do-Not-Restore");
			list.forEach(InventorySerializer::addNonSerializableElements);

			return !list.contains("health");
		});

		final String path;
		final boolean def;

		Option() {
			this(true);
		}

		Option(boolean def) {
			this.def = def;
			this.path = StringUtils.capitalize(name().replace('_', '-').toLowerCase(Locale.ENGLISH), '-', '.');
		}

		Option(String path) {
			this.def = true;
			this.path = path;
		}

		Option(DoubleSupplier<FileConfiguration, Boolean> supplier) {
			this.path = "";
			this.def = supplier.accept(JavaPlugin.getPlugin(Main.class).getConfig());
		}
	}
}
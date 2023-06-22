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

package me.despical.tntrun.handlers;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.Strings;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.User;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatManager {

	private final FileConfiguration config;

	public ChatManager(Main plugin) {
		this.config = ConfigUtils.getConfig(plugin, "messages");
	}

	public String message(final String path) {
		return rawMessage(this.config.getString(path));
	}

	public String message(final String path, final User user) {
		String message = this.message(path);

		message = message.replace("%player%", user.getPlayer().getName());
		return message;
	}

	public String message(final String path, final Arena arena, final User user) {
		String message = this.message(path, arena);

		message = message.replace("%player%", user.getPlayer().getName());

		return message;
	}

	public String message(final String path, final Arena arena) {
		String message = this.message(path);

		message = message.replace("%time%", Integer.toString(arena.getTimer()));
		message = message.replace("%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		message = message.replace("%players%", Integer.toString(arena.getPlayers().size()));
		message = message.replace("%players_left%", Integer.toString(arena.getPlayersLeft().size()));
		message = message.replace("%min_players%", Integer.toString(arena.getMinimumPlayers()));
		message = message.replace("%max_players%", Integer.toString(arena.getMaximumPlayers()));

		return message;
	}

	public ConfigurationSection getConfigurationSection(final String path) {
		return config.getConfigurationSection(path);
	}

	public String rawMessage(final String message) {
		return Strings.format(message);
	}

	public List<String> getStringList(final String path) {
		return this.config.getStringList(path);
	}
}
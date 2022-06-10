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

package me.despical.tntrun.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.Strings;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatManager {

	private String prefix;

	private final Main plugin;
	private FileConfiguration config;

	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = message("in-game.plugin-prefix");
	}

	public String getPrefix() {
		return prefix;
	}

	public String prefixedRawMessage(String message) {
		return prefix + color(message);
	}

	public String color(String message) {
		return Strings.format(message);
	}

	public String prefixedMessage(String path) {
		return prefix + message(path);
	}

	public String message(String path) {
		return color(config.getString(me.despical.commons.string.StringUtils.capitalize(path, '-', '.')));
	}

	public String message(String path, int integer) {
		return formatMessage(null, message(path), integer);
	}

	public String prefixedMessage(String path, int integer) {
		return prefix + message(path, integer);
	}

	public String prefixedMessage(String path, Player player) {
		return prefix + message(path, player);
	}

	public String message(String path, Player player) {
		String returnString = message(path);

		if (plugin.getConfigPreferences().isPapiEnabled()) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return color(returnString);
	}

	public String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%player%", player.getName());

		if (plugin.getConfigPreferences().isPapiEnabled()) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return formatPlaceholders(returnString, arena);
	}

	private String formatPlaceholders(String message, Arena arena) {
		String formatted = message;

		formatted = StringUtils.replace(formatted, "%arena%", arena.getMapName());
		formatted = StringUtils.replace(formatted, "%time%", Integer.toString(arena.getTimer()));
		formatted = StringUtils.replace(formatted, "%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
		formatted = StringUtils.replace(formatted, "%players%", Integer.toString(arena.getPlayersLeft().size()));
		formatted = StringUtils.replace(formatted, "%maxplayers%", Integer.toString(arena.getMaximumPlayers()));
		formatted = StringUtils.replace(formatted, "%minplayers%", Integer.toString(arena.getMinimumPlayers()));
		return color(formatted);
	}

	public String prefixedFormattedMessage(Arena arena, String path, int integer) {
		return prefix + formatMessage(arena, message(path), integer);
	}

	public String formatMessage(Arena arena, String message, int integer) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%number%", Integer.toString(integer));
		return arena != null ? formatPlaceholders(returnString, arena) : returnString;
	}

	public void broadcastAction(Arena arena, Player player, ActionType action) {
		arena.broadcastMessage(prefix + formatMessage(arena, message("in-game.messages." + action.name().toLowerCase(Locale.ENGLISH)), player));
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = message("in-game.plugin-prefix");
	}

	public enum ActionType {
		JOIN, LEAVE, DEATH
	}
}
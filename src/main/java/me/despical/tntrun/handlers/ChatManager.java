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

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatManager {

	private String prefix;

	private final Main plugin;
	private final boolean papi;
	private FileConfiguration config;

	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.papi = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholdersAPI");
		this.prefix = colorMessage("in-game.plugin-prefix");
	}

	public String getPrefix() {
		return prefix;
	}

	public String colorRawMessage(String message) {
		return Strings.format(message);
	}

	public String colorMessage(String path) {
		return colorRawMessage(config.getString(me.despical.commons.string.StringUtils.capitalize(path, '-', '.')));
	}

	public String colorMessage(String path, Player player) {
		String returnString = colorMessage(path);

		if (plugin.getConfigPreferences().isPapiEnabled()) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return colorRawMessage(returnString);
	}

	public String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%player%", player.getName());

		if (plugin.getConfigPreferences().isPapiEnabled()) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return colorRawMessage(formatPlaceholders(returnString, arena));
	}

	private String formatPlaceholders(String message, Arena arena) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%arena%", arena.getMapName());
		returnString = StringUtils.replace(returnString, "%time%", Integer.toString(arena.getTimer()));
		returnString = StringUtils.replace(returnString, "%formatted_time%", StringFormatUtils.formatIntoMMSS((arena.getTimer())));
		returnString = StringUtils.replace(returnString, "%players%", Integer.toString(arena.getPlayersLeft().size()));
		returnString = StringUtils.replace(returnString, "%maxplayers%", Integer.toString(arena.getMaximumPlayers()));
		returnString = StringUtils.replace(returnString, "%minplayers%", Integer.toString(arena.getMinimumPlayers()));
		return returnString;
	}

	public String formatMessage(Arena arena, String message, int integer) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%number%", Integer.toString(integer));
		returnString = colorRawMessage(formatPlaceholders(returnString, arena));
		return returnString;
	}

	public void broadcastAction(Arena arena, Player player, ActionType action) {
		arena.broadcastMessage(prefix + formatMessage(arena, colorMessage("in-game.messages." + action.name().toLowerCase()), player));
	}

	public List<String> getStringList(String path) {
		return config.getStringList(path);
	}

	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
		prefix = colorMessage("in-game.plugin-prefix");
	}

	public enum ActionType {
		JOIN, LEAVE, DEATH
	}
}
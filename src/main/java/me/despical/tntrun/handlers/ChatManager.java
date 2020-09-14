package me.despical.tntrun.handlers;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.string.StringFormatUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatManager {

	private final String prefix;
	
	private final Main plugin;
	private FileConfiguration config;
	
	public ChatManager(Main plugin) {
		this.plugin = plugin;
		this.config = ConfigUtils.getConfig(plugin, "messages");
		this.prefix = colorRawMessage(config.getString("In-Game.Plugin-Prefix", "&8[&6TNTRun&8] "));
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String colorRawMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String colorMessage(String message) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(message));
	}
	
	public String colorMessage(String message, Player player) {
		String returnString = config.getString(message);

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return ChatColor.translateAlternateColorCodes('&', returnString);
	}

	public String formatMessage(Arena arena, String message, Player player) {
		String returnString = message;
		returnString = StringUtils.replace(returnString, "%player%", player.getName());
		returnString = colorRawMessage(formatPlaceholders(returnString, arena));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			returnString = PlaceholderAPI.setPlaceholders(player, returnString);
		}

		return returnString;
	}

	private String formatPlaceholders(String message, Arena arena) {
		String returnString = message;

		returnString = StringUtils.replace(returnString, "%arena%", arena.getMapName());
		returnString = StringUtils.replace(returnString, "%time%", Integer.toString(arena.getTimer()));
		returnString = StringUtils.replace(returnString, "%formatted_time%", StringFormatUtils.formatIntoMMSS((arena.getTimer())));
		returnString = StringUtils.replace(returnString, "%players%", Integer.toString(arena.getPlayers().size()));
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

	public void broadcastAction(Arena a, Player p, ActionType action) {
		String message;
		switch (action) {
			case JOIN:
				message = formatMessage(a, colorMessage("In-Game.Messages.Join"), p);
				break;
			case LEAVE:
				message = formatMessage(a, colorMessage("In-Game.Messages.Leave"), p);
				break;
			case DEATH:
				message = formatMessage(a, colorMessage("In-Game.Messages.Death"), p);
				break;
			default:
				return;
		}

		a.getPlayers().forEach(player -> player.sendMessage(prefix + message));
	}
	
	public void reloadConfig() {
		config = ConfigUtils.getConfig(plugin, "messages");
	}

	public enum ActionType {
		JOIN, LEAVE, DEATH
	}
}
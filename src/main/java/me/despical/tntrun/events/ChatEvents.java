package me.despical.tntrun.events;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.user.User;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.regex.Pattern;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatEvents implements Listener {

	private final Main plugin;
	private final String[] regexChars = new String[] { "$", "\\" };

	public ChatEvents(Main plugin) {
		this.plugin = plugin;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(ignoreCancelled = true)
	public void onChatIngame(AsyncPlayerChatEvent event) {

		Arena arena = ArenaRegistry.getArena(event.getPlayer());
		if (arena == null) {
			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				for (Arena loopArena : ArenaRegistry.getArenas()) {
					for (Player player : loopArena.getPlayers()) {
						event.getRecipients().remove(player);
					}
				}
			}

			return;
		}

		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.CHAT_FORMAT_ENABLED)) {
			String eventMessage = event.getMessage();
			for (String regexChar : regexChars) {
				if (eventMessage.contains(regexChar)) {
					eventMessage = eventMessage.replaceAll(Pattern.quote(regexChar), "");
				}
			}

			String message = formatChatPlaceholders(plugin.getChatManager().colorMessage("In-Game.Game-Chat-Format"), plugin.getUserManager().getUser(event.getPlayer()), eventMessage);

			if (!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_SEPARATE_CHAT)) {
				event.setCancelled(true);

				boolean dead = !arena.getPlayersLeft().contains(event.getPlayer());

				for (Player player : arena.getPlayers()) {
					if (dead && arena.getPlayersLeft().contains(player)) {
						continue;
					}

					if (dead) {
						String prefix = formatChatPlaceholders(plugin.getChatManager().colorMessage("In-Game.Game-Death-Format"), plugin.getUserManager().getUser(event.getPlayer()), null);
						player.sendMessage(prefix + message);
					} else {
						player.sendMessage(message);
					}
				}

				Bukkit.getConsoleSender().sendMessage(message);
			} else {
				event.setMessage(message);
			}
		}
	}

	private String formatChatPlaceholders(String message, User user, String saidMessage) {
		String formatted = message;
		formatted = plugin.getChatManager().colorRawMessage(formatted);
		formatted = StringUtils.replace(formatted, "%player%", user.getPlayer().getName());
		formatted = StringUtils.replace(formatted, "%message%", ChatColor.stripColor(saidMessage));

		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			formatted = PlaceholderAPI.setPlaceholders(user.getPlayer(), formatted);
		}

		return formatted;
	}
}
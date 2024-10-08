/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.tntrun.utils;

import me.despical.commons.miscellaneous.DefaultFontInfo;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2023
 */
public class Utils {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);

	private Utils() {
	}

	public static void applyActionBarCooldown(final User user, int seconds) {
		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				final var arena = user.getArena();

				if (arena == null || arena.isDeathPlayer(user) || !arena.isArenaState(ArenaState.IN_GAME)) {
					cancel();
					return;
				}

				var progress = getProgressBar(ticks, seconds * 20);
				user.sendActionBar(plugin.getChatManager().message("messages.in-game.cooldown-format", user).replace("%progress%", progress).replace("%time%", Double.toString((double) ((seconds * 20) - ticks) / 20)));

				if (ticks >= seconds * 20) {
					cancel();
					return;
				}

				ticks += 2;
			}
		}.runTaskTimer(plugin, 0, 2);
	}

	private static String getProgressBar(int current, int max) {
		float percent = (float) current / max;
		int progressBars = (int) (10 * percent), leftOver = (10 - progressBars);
		String[] colors = plugin.getChatManager().message("messages.in-game.cooldown-progress-format").split(":");

		return "%s%s%s%s".formatted(colors[0], colors[2].repeat(Math.max(0, progressBars)), colors[1], colors[2].repeat(Math.max(0, leftOver)));
	}

	public static void sendCenteredMessage(CommandSender sender, BaseComponent[] components) {
		BaseComponent[] message;
		String[] lines = org.bukkit.ChatColor.translateAlternateColorCodes('&', BaseComponent.toLegacyText(components)).split("\n", 40);
		StringBuilder returnMessage = new StringBuilder();
		String[] linesCopy = lines;
		int length = lines.length;

		for (int i = 0; i < length; ++i) {
			String line = linesCopy[i];

			if (line.contains("%no_center%")) {
				continue;
			}

			int messagePxSize = 0;
			boolean previousCode = false, isBold = false;
			char[] array = line.toCharArray();
			int spaceLength = array.length, compensated;

			for (compensated = 0; compensated < spaceLength; ++compensated) {
				char c = array[compensated];

				if (c == 167) {
					previousCode = true;
				} else if (previousCode) {
					previousCode = false;
					isBold = c == 'l';
				} else {
					DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
					messagePxSize = isBold ? messagePxSize + dFI.getBoldLength() : messagePxSize + dFI.getLength();
					++messagePxSize;
				}
			}

			int toCompensate = 165 - messagePxSize / 2;
			spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
			compensated = 0;

			StringBuilder sb;

			for (sb = new StringBuilder(); compensated < toCompensate; compensated += spaceLength) {
				sb.append(" ");
			}

			returnMessage.append(sb);
		}


		message = new ComponentBuilder()
			.append(returnMessage.toString().replace("%no_center%", ""))
			.append(components)
			.create();

		sender.spigot().sendMessage(message);
	}
}
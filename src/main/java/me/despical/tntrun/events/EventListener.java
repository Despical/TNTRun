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

package me.despical.tntrun.events;

import me.despical.tntrun.Main;
import me.despical.tntrun.events.event.GameEvents;
import me.despical.tntrun.events.event.GameItemEvents;
import me.despical.tntrun.events.event.JoinQuitEvents;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.user.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public abstract class EventListener implements Listener {

	protected final Main plugin;
	protected final ChatManager chatManager;
	protected final UserManager userManager;

	public EventListener(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.userManager = plugin.getUserManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static void registerEvents(Main plugin) {
		new JoinQuitEvents(plugin);
		new GameItemEvents(plugin);
		new GameEvents(plugin);
	}

	protected final boolean isInArena(Player player) {
		return this.userManager.getUser(player).isInArena();
	}
}
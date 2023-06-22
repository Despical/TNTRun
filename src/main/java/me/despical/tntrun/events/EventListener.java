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

package me.despical.tntrun.events;

import me.despical.tntrun.Main;
import me.despical.tntrun.events.event.GameEvents;
import me.despical.tntrun.events.event.GameItemEvents;
import me.despical.tntrun.events.event.JoinQuitEvents;
import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public abstract class EventListener implements Listener {

	@NotNull
	protected final Main plugin;

	@NotNull
	protected final ChatManager chatManager;

	public EventListener(@NotNull Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public static void registerEvents(final Main plugin) {
		final Class<?>[] listenerAdapters = {JoinQuitEvents.class, GameItemEvents.class, GameEvents.class};

		try {
			for (Class<?> listenerAdapter : listenerAdapters) {
				listenerAdapter.getConstructor(Main.class).newInstance(plugin);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}
}
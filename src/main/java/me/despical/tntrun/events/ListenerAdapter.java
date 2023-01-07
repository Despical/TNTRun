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

import me.despical.commons.util.LogUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.ArenaEvents;
import me.despical.tntrun.events.spectator.SpectatorEvents;
import me.despical.tntrun.events.spectator.SpectatorItemEvents;
import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.event.Listener;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public abstract class ListenerAdapter implements Listener {

	protected final Main plugin;
	protected final ChatManager chatManager;

	public ListenerAdapter(Main plugin) {
		this.plugin = plugin;
		this.chatManager = plugin.getChatManager();
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	protected void registerIf(Predicate<Boolean> predicate, Supplier<Listener> supplier) {
		if (predicate.test(false)) return;

		plugin.getServer().getPluginManager().registerEvents(supplier.get(), plugin);
	}

	public static void registerEvents(Main plugin) {
		final Class<?>[] listenerAdapters = {Events.class, ChatEvents.class, SpectatorEvents.class, SpectatorItemEvents.class, ArenaEvents.class};

		try {
			for (Class<?> listenerAdapter : listenerAdapters) {
				listenerAdapter.getConstructor(Main.class).newInstance(plugin);
			}
		} catch (Exception ignored) {
			LogUtils.sendConsoleMessage("&cAn exception occured on event registering.");
		}
	}
}
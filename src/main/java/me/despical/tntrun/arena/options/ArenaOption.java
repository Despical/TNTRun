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

package me.despical.tntrun.arena.options;

import me.despical.tntrun.Main;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public enum ArenaOption {

	ID(""),

	TIMER(45),

	READY(false),

	FORCE_START(false),

	STOPPED(false),

	MAP_NAME(""),

	MINIMUM_PLAYERS(2),

	MAXIMUM_PLAYERS(12),

	MIN_DEPTH("Scanning-Depth.On-Ground", 2),

	MAX_DEPTH("Scanning-Depth.In-Air", 6),

	START_BLOCK_REMOVING("Time-Settings.Start-Block-Removing", 5),

	BLOCK_REMOVE_DELAY("Time-Settings.Block-Remove-Delay", 12),

	LOBBY_WAITING_TIME("Time-Settings.Lobby-Waiting-Time", 45),

	LOBBY_STARTING_TIME("Time-Settings.Lobby-Starting-Time", 16),

	LOBBY_ENDING_TIME("Time-Settings.Ending-Time", 6),

	LOBBY_LOCATION(null),

	END_LOCATION(null);

	Object value;

	ArenaOption(Object value) {
		this.value = value;
	}

	ArenaOption(String path, Object defaultValue) {
		final var plugin = JavaPlugin.getPlugin(Main.class);

		this.value = plugin.getConfig().get(path, defaultValue);
	}

	@SuppressWarnings("unchecked")
	public <T> T getOption() {
		return (T) this.value;
	}
}
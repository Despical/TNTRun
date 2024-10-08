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

package me.despical.tntrun.arena.managers;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.data.ArenaData;
import me.despical.tntrun.user.User;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public final class GameBarManager {

	private final BossBar gameBar;
	private final ArenaData data;
	private final Main plugin;

	public GameBarManager(final ArenaData data, final Main plugin) {
		this.data = data;
		this.plugin = plugin;
		this.gameBar = plugin.getOption(ConfigPreferences.Option.GAME_BAR_ENABLED) ? plugin.getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID) : null;
	}

	public void doBarAction(final User user, int action) {
		if (this.gameBar == null) return;

		final var player = user.getPlayer();

		if (action == 1) {
			this.gameBar.addPlayer(player);
		} else {
			this.gameBar.removePlayer(player);
		}
	}

	public void removeAll() {
		if (this.gameBar != null) this.gameBar.removeAll();
	}

	public void handleGameBar() {
		if (this.gameBar == null) return;

		switch (data.getArenaState()) {
			case WAITING_FOR_PLAYERS -> updateState("waiting-for-players");
			case STARTING -> updateState("starting");
			case IN_GAME -> updateState("in-game");
			case ENDING -> updateState("ending");
		}

		gameBar.setVisible(!this.gameBar.getTitle().isEmpty());
	}

	private void updateState(String path) {
		path = "game-bar.%s.".formatted(path);

		this.gameBar.setTitle(plugin.getChatManager().message(path + "message"));
		this.gameBar.setColor(this.getBarColor(path + "color"));
		this.gameBar.setStyle(this.getBarStyle(path + "style"));
	}

	private BarColor getBarColor(String path) {
		final String colorName = plugin.getChatManager().message(path);

		return colorName.isEmpty() ? this.gameBar.getColor() : BarColor.valueOf(colorName);
	}

	private BarStyle getBarStyle(String path) {
		final String colorName = plugin.getChatManager().message(path);

		return colorName.isEmpty() ? this.gameBar.getStyle() : BarStyle.valueOf(colorName);
	}
}
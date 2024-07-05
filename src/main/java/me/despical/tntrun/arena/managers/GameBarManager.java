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
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.User;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.jetbrains.annotations.Nullable;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class GameBarManager {

	@Nullable
	private BossBar gameBar;

	private final Arena arena;
	private final Main plugin;
	private final boolean enabled;

	public GameBarManager(final Arena arena, final Main plugin) {
		this.arena = arena;
		this.plugin = plugin;
		this.enabled = plugin.getOption(ConfigPreferences.Option.GAME_BAR_ENABLED);

		if (enabled) {
			this.gameBar = plugin.getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID);
		}
	}

	public void doBarAction(final User user, int action) {
		if (!enabled) return;

		final var player = user.getPlayer();

		if (action == 1) {
			this.gameBar.addPlayer(player);
		} else {
			this.gameBar.removePlayer(player);
		}
	}

	public void removeAll() {
		if (this.gameBar != null)
			this.gameBar.removeAll();
	}

	public void handleGameBar() {
		if (this.gameBar == null) return;

		switch (arena.getArenaState()) {
			case WAITING_FOR_PLAYERS -> setTitle("game-bar.waiting-for-players");
			case STARTING -> setTitle("game-bar.starting");
			case IN_GAME -> setTitle("game-bar.in-game");
			case ENDING -> setTitle("game-bar.ending");
		}

		gameBar.setVisible(!this.gameBar.getTitle().isEmpty());
	}

	private void setTitle(final String path) {
		this.gameBar.setTitle(plugin.getChatManager().message(path));
	}
}
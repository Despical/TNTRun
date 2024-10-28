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

package me.despical.tntrun.handlers;

import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.user.User;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PermissionsManager {

	private final int defaultDoubleJumps, doubleJumpDelay;
	private final String joinPermission, fullJoin;
	private final List<String> doubleJumpsPerms;

	public PermissionsManager(Main plugin) {
		final var config = plugin.getConfig();

		this.defaultDoubleJumps = config.getInt("Double-Jumps.Default", 5);
		this.doubleJumpDelay = config.getInt("Double-Jumps.Delay", 4);
		this.joinPermission = config.getString("Join-Permission", "");
		this.fullJoin = config.getString("Join-Full-Game-Permission", "");
		this.doubleJumpsPerms = config.getStringList("Double-Jumps.Permissions");
	}

	public boolean hasPermission(final User user, final Arena arena) {
		return joinPermission.isEmpty() || user.hasPermission(joinPermission.replace("<arena>", arena.getId()));
	}

	public boolean hasFullGamePerm(Player player) {
		return fullJoin.isEmpty() || player.hasPermission(fullJoin);
	}

	public int getDoubleJumpDelay() {
		return doubleJumpDelay;
	}

	public int getDoubleJumps(Player player) {
		for (String perm : doubleJumpsPerms) {
			if (perm.startsWith("tntrun") && player.hasPermission(perm)) {
				return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
			}
		}

		return defaultDoubleJumps;
	}
}

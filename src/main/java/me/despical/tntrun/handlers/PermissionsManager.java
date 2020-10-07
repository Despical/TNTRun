/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

import me.despical.tntrun.utils.Debugger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.despical.tntrun.Main;

/**
* @author Despical
* <p>
* Created at 10.07.2020
*/
public class PermissionsManager {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static String joinFullPerm = "tntrun.fullgames";
	private static String joinPerm = "tntrun.join.<arena>";

	public static void init() {
		setupPermissions();
	}

	public static String getJoinFullGames() {
		return joinFullPerm;
	}

	private static void setJoinFullGames(String joinFullGames) {
		PermissionsManager.joinFullPerm = joinFullGames;
	}

	public static String getJoinPerm() {
		return joinPerm;
	}

	private static void setJoinPerm(String joinPerm) {
		PermissionsManager.joinPerm = joinPerm;
	}

	private static void setupPermissions() {
		PermissionsManager.setJoinFullGames(plugin.getConfig().getString("Basic-Permissions.Full-Games-Permission", "tntrun.fullgames"));
		PermissionsManager.setJoinPerm(plugin.getConfig().getString("Basic-Permissions.Join-Permission", "tntrun.join.<arena>"));

		Debugger.debug("Basic permissions registered");
	}

	public static int getDoubleJumps(Player player) {
		for (String perm : plugin.getConfig().getStringList("Double-Jumps")) {
            if (perm.startsWith("tntrun") && player.hasPermission(perm)) {
            	return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
            }
        }

		return plugin.getConfig().getInt("Default-Double-Jumps", 5);
	}
}

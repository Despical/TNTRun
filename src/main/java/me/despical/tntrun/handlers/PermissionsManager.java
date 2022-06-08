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

import me.despical.tntrun.Main;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class PermissionsManager {

	private final Main plugin;
	private final String joinPerm, joinFullPerm;

	public PermissionsManager(Main plugin) {
		this.plugin = plugin;
		this.joinPerm = plugin.getConfig().getString("Basic-Permissions.Join-Permission", "tntrun.join.<arena>");
		this.joinFullPerm = plugin.getConfig().getString("Basic-Permissions.Full-Games-Permission", "tntrun.fullgames");
	}

	public boolean hasJoinPerm(Player player, String replacement) {
		return player.hasPermission(this.joinPerm.replace("<arena>", replacement));
	}

	public boolean hasFullPerm(Player player) {
		return player.hasPermission(this.joinFullPerm);
	}

	public boolean hasNotifyPerm(Player player) {
		return plugin.getConfig().getBoolean("Update-Notifier.Enabled", true) && player.hasPermission("tntrun.updatenotify");
	}

	public String getJoinPerm() {
		return this.joinPerm;
	}

	public int getDoubleJumps(Player player) {
		for (String perm : plugin.getConfig().getStringList("Double-Jumps")) {
			if (perm.startsWith("tntrun") && player.hasPermission(perm)) {
				return Integer.parseInt(perm.substring(perm.lastIndexOf('.') + 1));
			}
		}

		return plugin.getConfig().getInt("Default-Double-Jumps", 5);
	}
}

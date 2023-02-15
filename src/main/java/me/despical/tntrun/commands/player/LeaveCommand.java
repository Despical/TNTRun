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

package me.despical.tntrun.commands.player;

import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class LeaveCommand extends SubCommand {

	public LeaveCommand() {
		super ("leave");
	}

	@Override
	public String getPossibleArguments() {
		return null;
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_LEAVE_COMMAND)) {
			return;
		}

		Player player = (Player) sender;
		Arena arena = ArenaRegistry.getArena(player);

		if (arena == null) {
			player.sendMessage(chatManager.prefixedMessage("commands.not_playing", player));
			return;
		}

		player.sendMessage(chatManager.prefixedMessage("commands.teleported_to_the_lobby", player));

		ArenaManager.leaveAttempt(player, arena);
	}

	@Override
	public String getTutorial() {
		return null;
	}

	@Override
	public int getType() {
		return HIDDEN;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}
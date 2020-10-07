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

package me.despical.tntrun.commands.admin.arena;

import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

import static me.despical.tntrun.arena.ArenaRegistry.getArena;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class StopCommand extends SubCommand {

	public StopCommand() {
		super("stop");
		setPermission("tntrun.admin.stop");
	}

	@Override
	public String getPossibleArguments() {
		return "";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (!Utils.checkIsInGameInstance((Player) sender)) {
			return;
		}

		if (getArena((Player) sender).getArenaState() != ArenaState.ENDING) {
			ArenaManager.stopGame(true, getArena((Player) sender));
		}
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Stop the arena you're in", "You must be in target arena!");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}
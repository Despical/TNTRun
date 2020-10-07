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

package me.despical.tntrun.commands.game;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;

/**
 * @author Despical
 * <p>
 * Created at 02.07.2020
 */
public class JoinCommand extends SubCommand {

	public JoinCommand() {
		super("join");
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
		if (args.length == 0) {
			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Type-Arena-Name"));
			return;
		}

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (args[0].equalsIgnoreCase(arena.getId())) {
				ArenaManager.joinAttempt((Player) sender, arena);
				return;
			}
		}

		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.No-Arena-Like-That"));
	}

	@Override
	public List<String> getTutorial() {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.HIDDEN;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.PLAYER;
	}
}
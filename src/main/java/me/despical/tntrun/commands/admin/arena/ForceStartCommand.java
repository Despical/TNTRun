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

import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ForceStartCommand extends SubCommand {

	public ForceStartCommand() {
		super("forcestart");

		setPermission("tntrun.admin.forcestart");
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
	public void execute(CommandSender sender, String[] args) {
		if (!checkIsInGameInstance((Player) sender)) {
			return;
		}

		Arena arena = ArenaRegistry.getArena((Player) sender);

		if (arena.getPlayers().size() < 2) {
			arena.broadcastMessage(plugin.getChatManager().formatMessage(arena, plugin.getChatManager().colorMessage("In-Game.Messages.Lobby-Messages.Waiting-For-Players"), arena.getMinimumPlayers()));
			return;
		}

		if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
			arena.setArenaState(ArenaState.STARTING);
			arena.setForceStart(true);
			arena.setTimer(0);
			arena.broadcastMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("In-Game.Messages.Admin-Messages.Set-Starting-In-To-0"));
		}
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Force start arena you're in");
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
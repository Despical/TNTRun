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

import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class EditCommand extends SubCommand {

	public EditCommand() {
		super("edit");

		setPermission("tntrun.admin.setup");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (ArenaRegistry.getArena(args[0]) == null) {
			sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}

		new SetupInventory(ArenaRegistry.getArena(args[0]), (Player) sender).openInventory();
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Open arena editor menu");
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
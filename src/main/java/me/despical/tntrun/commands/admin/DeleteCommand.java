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

package me.despical.tntrun.commands.admin;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class DeleteCommand extends SubCommand {

	private final Set<CommandSender> confirmations;

	public DeleteCommand() {
		super ("delete");
		this.confirmations = new HashSet<>();

		setPermission("tntrun.admin.delete");
	}

	@Override
	public String getPossibleArguments() {
		return "<arena>";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		String arenaName = args[0];
		Arena arena = ArenaRegistry.getArena(arenaName);

		if (arena == null) {
			sender.sendMessage(chatManager.prefixedMessage("commands.no_arena_like_that"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			plugin.getServer().getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 200);
			sender.sendMessage(chatManager.prefixedMessage("commands.are_you_sure"));
			return;
		}

		confirmations.remove(sender);

		ArenaManager.stopGame(true, arena);
		ArenaRegistry.unregisterArena(arena);

		config.set("instances." + arenaName, null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		plugin.getSignManager().loadSigns();

		sender.sendMessage(chatManager.prefixedMessage("commands.removed_game_instance"));
	}

	@Override
	public String getTutorial() {
		return "Deletes arena with the current configuration";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return BOTH;
	}
}
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

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class DeleteCommand extends SubCommand {

	private final Set<CommandSender> confirmations = new HashSet<>();

	public DeleteCommand() {
		super("delete");

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
	public void execute(CommandSender sender, String[] args) {
		if (args.length == 0) {
			sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Type-Arena-Name"));
			return;
		}

		Arena arena = ArenaRegistry.getArena(args[0]);

		if (arena == null) {
			sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.No-Arena-Like-That"));
			return;
		}

		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(plugin, () -> confirmations.remove(sender), 20 * 10);

			sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}

		confirmations.remove(sender);
		ArenaManager.stopGame(true, arena);
		plugin.getSignManager().getArenaSigns().remove(plugin.getSignManager().getArenaSignByArena(arena));
		ArenaRegistry.unregisterArena(arena);

		FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

		config.set("instances." + args[0], null);
		ConfigUtils.saveConfig(plugin, config, "arenas");

		sender.sendMessage(plugin.getChatManager().getPrefix() + plugin.getChatManager().colorMessage("Commands.Removed-Game-Instance"));
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Delete specified arena");
	}

	@Override
	public CommandType getType() {
		return CommandType.GENERIC;
	}

	@Override
	public SenderType getSenderType() {
		return SenderType.BOTH;
	}
}
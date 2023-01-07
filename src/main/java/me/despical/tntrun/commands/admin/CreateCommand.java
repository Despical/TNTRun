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
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super ("create");

		setPermission("tntrun.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<id>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public void execute(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;

		if (args.length == 0) {
			player.sendMessage(chatManager.prefixedMessage("commands.type_arena_name"));
			return;
		}

		String id = args[0];

		if (ArenaRegistry.isArena(id)) {
			player.sendMessage(chatManager.prefixedRawMessage("&cArena with that ID already contains!"));
			player.sendMessage(chatManager.prefixedRawMessage("&cTo check existing arenas use: /tr list"));
			return;
		}

		player.sendMessage(chatManager.color("&l--------------------------------------------"));
		MiscUtils.sendCenteredMessage(player, "&eInstance &a&l" + id + " &ecreated!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&aEdit this arena via /tr edit &6" + id + "&a!");
		player.sendMessage("");
		MiscUtils.sendCenteredMessage(player, "&6Don't know where to start? Check out our wiki:");
		MiscUtils.sendCenteredMessage(player, "&7https://www.github.com/Despical/TNTRun/wiki");
		player.sendMessage(chatManager.color("&l--------------------------------------------"));

		// Configuration setup and registering arena
		String path = "instances." + id + ".";
		config.set(path + "ready", false);
		config.set(path + "mapName", id);
		config.set(path + "minimumPlayers", 2);
		config.set(path + "maximumPlayers", 12);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "lobbyLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "signs", new ArrayList<>());

		ConfigUtils.saveConfig(plugin, config, "arenas");

		Arena arena = new Arena(id);
		arena.setMapName(id);
		arena.setReady(false);
		arena.setLobbyLocation(LocationSerializer.DEFAULT_LOCATION);
		arena.setEndLocation(LocationSerializer.DEFAULT_LOCATION);

		ArenaRegistry.registerArena(arena);
	}

	@Override
	public String getTutorial() {
		return "Creates a new arena with default configuration";
	}

	@Override
	public int getType() {
		return GENERIC;
	}

	@Override
	public int getSenderType() {
		return PLAYER;
	}
}
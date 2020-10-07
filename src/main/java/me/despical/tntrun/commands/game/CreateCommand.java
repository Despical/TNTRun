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

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.serializer.LocationSerializer;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 02.07.2020
 */
public class CreateCommand extends SubCommand {

	public CreateCommand() {
		super("create");
		setPermission("tntrun.admin.create");
	}

	@Override
	public String getPossibleArguments() {
		return "<ID>";
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

		Player player = (Player) sender;

		for (Arena arena : ArenaRegistry.getArenas()) {
			if (arena.getId().equalsIgnoreCase(args[0])) {
				player.sendMessage(getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Arena with that ID already exists!");
				player.sendMessage(getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Usage: /tntrun create <ID>");
				return;
			}
		}

		if (ConfigUtils.getConfig(getPlugin(), "arenas").contains("instances." + args[0])) {
			player.sendMessage(getPlugin().getChatManager().getPrefix() + ChatColor.RED + "Instance/Arena already exists! Use another ID or delete it first!");
		} else {
			createInstanceInConfig(args[0]);

			player.sendMessage(ChatColor.BOLD + "----------------------------------------");
			player.sendMessage(ChatColor.YELLOW + "      Instance " + args[0] + " created!");
			player.sendMessage("");
			player.sendMessage(ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/" + label + " edit " + args[0] + ChatColor.GREEN + "!");
			player.sendMessage(ChatColor.BOLD + "----------------------------------------");
		}
	}

	private void createInstanceInConfig(String id) {
		String path = "instances." + id + ".";
		FileConfiguration config = ConfigUtils.getConfig(getPlugin(), "arenas");

		config.set(path + "lobbylocation", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "Endlocation", LocationSerializer.locationToString(Bukkit.getServer().getWorlds().get(0).getSpawnLocation()));
		config.set(path + "minimumplayers", 2);
		config.set(path + "maximumplayers", 12);
		config.set(path + "mapname", id);
		config.set(path + "signs", new ArrayList<>());
		config.set(path + "isdone", false);
		ConfigUtils.saveConfig(getPlugin(), config, "arenas");

		Arena arena = new Arena(id);

		arena.setMapName(config.getString(path + "mapname"));
		arena.setLobbyLocation(LocationSerializer.locationFromString(config.getString(path + "lobbylocation")));
		arena.setEndLocation(LocationSerializer.locationFromString(config.getString(path + "Endlocation")));
		arena.setReady(false);

		ArenaRegistry.registerArena(arena);
	}

	@Override
	public List<String> getTutorial() {
		return Collections.singletonList("Create new arena");
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
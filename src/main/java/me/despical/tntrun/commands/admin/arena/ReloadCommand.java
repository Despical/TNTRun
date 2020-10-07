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

import me.despical.commonsbox.serializer.InventorySerializer;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import me.despical.tntrun.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ReloadCommand extends SubCommand {

	private final Set<CommandSender> confirmations = new HashSet<>();

	public ReloadCommand() {
		super("reload");
		setPermission("tntrun.admin.reload");
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
		if (!confirmations.contains(sender)) {
			confirmations.add(sender);
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> confirmations.remove(sender), 20 * 10);

			sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Are-You-Sure"));
			return;
		}

		confirmations.remove(sender);
		Debugger.debug("Initiated plugin reload by {0}", sender.getName());

		long start = System.currentTimeMillis();

		getPlugin().reloadConfig();
		getPlugin().getChatManager().reloadConfig();

		for (Arena arena : ArenaRegistry.getArenas()) {
			Debugger.debug("[Reloader] Stopping {0} instance.");

			long stopTime = System.currentTimeMillis();

			for (Player player : arena.getPlayers()) {
				arena.doBarAction(Arena.BarAction.REMOVE, player);

				if (getPlugin().getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
					InventorySerializer.loadInventory(getPlugin(), player);
				} else {
					player.getInventory().clear();
					player.getInventory().setArmorContents(null);
					player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
					player.setWalkSpeed(0.2f);
				}
			}

			ArenaManager.stopGame(true, arena);
			Debugger.debug("[Reloader] Instance {0} stopped took {1} ms", arena.getId(), System.currentTimeMillis() - stopTime);
		}

		ArenaRegistry.registerArenas();

		sender.sendMessage(getPlugin().getChatManager().getPrefix() + getPlugin().getChatManager().colorMessage("Commands.Admin-Commands.Success-Reload"));
		Debugger.debug("[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public List<String> getTutorial() {
		return Arrays.asList("Reload all game arenas and configurations", "All of the arenas will be stopped!");
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
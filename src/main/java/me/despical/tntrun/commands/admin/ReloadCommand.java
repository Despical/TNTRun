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

import me.despical.commons.util.LogUtils;
import me.despical.tntrun.arena.ArenaManager;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.commands.SubCommand;
import org.bukkit.command.CommandSender;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public class ReloadCommand extends SubCommand {

	public ReloadCommand() {
		super ("reload");

		setPermission("tntrun.admin.reload");
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
		LogUtils.log("Initiated plugin reload by {0}", sender.getName());
		long start = System.currentTimeMillis();

		plugin.reloadConfig();
		chatManager.reloadConfig();

		ArenaRegistry.getArenas().forEach(arena -> ArenaManager.stopGame(true, arena));
		ArenaRegistry.registerArenas();

		sender.sendMessage(chatManager.prefixedMessage("commands.admin_commands.success_reload"));

		LogUtils.log("[Reloader] Finished reloading took {0} ms", System.currentTimeMillis() - start);
	}

	@Override
	public String getTutorial() {
		return "Reloads all of the system configuration and arenas";
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
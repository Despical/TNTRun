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

package me.despical.tntrun.commands;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 14.07.2022
 */
public abstract class SubCommand {

	private String permission;
	private final String name;

	protected final Main plugin;
	protected final FileConfiguration config;
	protected final ChatManager chatManager;
	protected final int GENERIC = 0, HIDDEN = 1, PLAYER = 0, BOTH = 1;

	public SubCommand(String name) {
		this.name = name;
		this.plugin = JavaPlugin.getPlugin(Main.class);
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.chatManager = plugin.getChatManager();
	}

	public String getName() {
		return name;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public final boolean hasPermission(CommandSender sender) {
		return permission == null || sender.hasPermission(permission);
	}

	public abstract String getPossibleArguments();

	public abstract int getMinimumArguments();

	public abstract void execute(CommandSender sender, String label, String[] args);

	public abstract String getTutorial();

	public abstract int getType();

	public abstract int getSenderType();
}
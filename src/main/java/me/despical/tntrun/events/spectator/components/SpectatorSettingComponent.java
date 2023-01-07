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

package me.despical.tntrun.events.spectator.components;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.events.spectator.SpectatorSettingsMenu;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 05.10.2020
 */
public interface SpectatorSettingComponent {

	Main plugin = JavaPlugin.getPlugin(Main.class);
	ChatManager chatManager = plugin.getChatManager();
	FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");

	void registerComponent(SpectatorSettingsMenu spectatorSettingsMenu, StaticPane pane);
}
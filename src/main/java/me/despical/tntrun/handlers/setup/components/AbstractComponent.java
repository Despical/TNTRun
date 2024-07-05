/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.tntrun.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import me.despical.tntrun.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public abstract class AbstractComponent {

	protected static final ItemStack mainMenuItem = new ItemBuilder(XMaterial.REDSTONE).name("&c&lReturn to Main Menu").build();

	protected final ArenaEditorGUI gui;
	protected final User user;
	protected final String path;
	protected final Arena arena;
	protected final Main plugin;

	public AbstractComponent(final ArenaEditorGUI gui) {
		this.gui = gui;
		this.user = gui.getUser();
		this.arena = gui.getArena();
		this.path = "instance.%s.".formatted(arena);
		this.plugin = gui.getPlugin();
	}

	public abstract void registerComponents(final PaginatedPane paginatedPane);

	protected String isOptionDone(String path, FileConfiguration config) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) ? "&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)" : "&c&l✘ Not Completed";
	}

	protected String isOptionDoneBool(String path, FileConfiguration config) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}

	protected boolean isOptionDoneBoolean(String path, FileConfiguration config) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) && !LocationSerializer.isDefaultLocation(config.getString(path));
	}

	protected int minValueHigherThan(String path, int higher, FileConfiguration config) {
		path = "instance.%s.%s".formatted(arena, path);

		return Math.max(higher, config.getInt(path));
	}
}
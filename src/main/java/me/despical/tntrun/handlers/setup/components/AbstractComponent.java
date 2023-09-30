package me.despical.tntrun.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
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

	protected final ArenaEditorGUI gui;
	protected final User user;
	protected final String path;
	protected final Arena arena;
	protected final Main plugin;
	protected final FileConfiguration config;

	protected static final ItemStack mainMenuItem = new ItemBuilder(XMaterial.REDSTONE).name("&c&lReturn TNT Run Menu").lore("&7Click to return last page!").build();

	public AbstractComponent(final ArenaEditorGUI gui) {
		this.gui = gui;
		this.user = gui.getUser();
		this.arena = gui.getArena();
		this.path = "instance.%s.".formatted(gui.getArena().getId());
		this.plugin = gui.getPlugin();
		this.config = ConfigUtils.getConfig(plugin, "arena");
	}

	public abstract void registerComponents(final PaginatedPane paginatedPane);

	protected String isOptionDone(String path) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) ? "&a&l✔ Completed &7(value: &8" + config.getString(path) + "&7)" : "&c&l✘ Not Completed";
	}

	protected String isOptionDoneBool(String path) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) ? LocationSerializer.isDefaultLocation(config.getString(path)) ? "&c&l✘ Not Completed" : "&a&l✔ Completed" : "&c&l✘ Not Completed";
	}

	protected boolean isOptionDoneBoolean(String path) {
		path = "instance.%s.%s".formatted(arena, path);

		return config.isSet(path) && !LocationSerializer.isDefaultLocation(config.getString(path));
	}

	protected int minValueHigherThan(String path, int higher) {
		path = "instance.%s.%s".formatted(arena, path);

		return Math.max(higher, config.getInt(path));
	}

	protected void saveConfig() {
		ConfigUtils.saveConfig(plugin, this.config, "arena");
	}
}
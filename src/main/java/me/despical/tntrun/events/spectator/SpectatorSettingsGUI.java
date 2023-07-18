package me.despical.tntrun.events.spectator;

import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiBuilder;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.events.spectator.components.SettingComponents;
import me.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class SpectatorSettingsGUI {

	@NotNull
	private final Main plugin;

	@NotNull
	private final User user;

	@NotNull
	private final Arena arena;

	@NotNull
	private final Gui gui;

	public SpectatorSettingsGUI(final @NotNull Main plugin, final @NotNull User user, final @NotNull Arena arena) {
		this.plugin = plugin;
		this.user = user;
		this.arena = arena;

		final var pane = new StaticPane(9, 4);
		this.gui = new GuiBuilder(plugin, 4, plugin.getChatManager().message("spectator-gui.title")).globalClick(event -> event.setCancelled(true)).pane(pane).build();

		this.registerComponents(pane);
	}

	private void registerComponents(final StaticPane pane) {
		final var mainComponents = new SettingComponents();
		mainComponents.registerComponents(this, pane);
	}

	public void showGui() {
		this.gui.show(this.user.getPlayer());
	}

	@NotNull
	public Main getPlugin() {
		return plugin;
	}

	@NotNull
	public Arena getArena() {
		return arena;
	}

	@NotNull
	public User getUser() {
		return user;
	}
}
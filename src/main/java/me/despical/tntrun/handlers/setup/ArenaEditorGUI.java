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

package me.despical.tntrun.handlers.setup;

import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.components.component.LobbyLocationComponents;
import me.despical.tntrun.handlers.setup.components.component.MainMenuComponents;
import me.despical.tntrun.handlers.setup.components.component.PlayerAmountComponents;
import me.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ArenaEditorGUI {

	@NotNull
	private final User user;

	@NotNull
	private final Main plugin;

	@NotNull
	private final Gui gui;

	@NotNull
	private final Arena arena;

	@NotNull
	private final PaginatedPane paginatedPane;

	public ArenaEditorGUI(final @NotNull Main plugin, final @NotNull User user, final @NotNull Arena arena) {
		this.plugin = plugin;
		this.user = user;
		this.arena = arena;
		this.gui = new Gui(plugin, 4, "       TNT Run Arena Editor");
		this.paginatedPane = new PaginatedPane(9, 4);
		this.gui.setOnGlobalClick(event -> event.setCancelled(true));
		this.gui.addPane(paginatedPane);

		this.injectGuiComponents();
	}

	private void injectGuiComponents() {
		final var mainMenuComponents = new MainMenuComponents(this);
		mainMenuComponents.registerComponents(paginatedPane);

		final var lobbyLocationComponents = new LobbyLocationComponents(this);
		lobbyLocationComponents.registerComponents(paginatedPane);

		final var playerAmountComponents = new PlayerAmountComponents(this);
		playerAmountComponents.registerComponents(paginatedPane);
	}

	public void showGui() {
		this.gui.show(this.user.getPlayer());
	}

	public void showGui(@Nullable String title, int rows, int page) {
		this.setPage(title, rows, page, false);

		this.showGui();
	}

	public void showGuiFromPage(final Page page) {
		this.setPage(page.title, page.rows, page.page, true);

		this.gui.show(this.user.getPlayer());
	}

	public void setPage(@Nullable String title, int rows, int page) {
		this.setPage(title, rows, page, true);
	}

	private void setPage(@Nullable String title, int rows, int page, boolean update) {
		this.gui.setTitle(title != null ? title : this.gui.getTitle());
		this.gui.setRows(rows);
		this.paginatedPane.setPage(page);

		if (update) this.gui.update();
	}

	public void restorePage() {
		paginatedPane.setPage(0);
		gui.setRows(4);
		gui.setTitle("    TNT Run Arena Editor");
		gui.update();
	}

	@NotNull
	public Main getPlugin() {
		return plugin;
	}

	@NotNull
	public User getUser() {
		return user;
	}

	@NotNull
	public Arena getArena() {
		return arena;
	}

	@NotNull
	public Gui getGui() {
		return gui;
	}

	public record Page(Arena arena, String title, int rows, int page) {

	}
}
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

package me.despical.tntrun.events.spectator;

import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiBuilder;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.events.spectator.components.TeleporterComponents;
import me.despical.tntrun.user.User;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class SpectatorTeleporterGUI {

    @NotNull
    private final Main plugin;

    @NotNull
    private final User user;

    @NotNull
    private final Arena arena;

    @NotNull
    private final Gui gui;

    public SpectatorTeleporterGUI(@NotNull Main plugin, @NotNull User user, @NotNull Arena arena) {
        this.plugin = plugin;
        this.user = user;
        this.arena = arena;

        var pane = new StaticPane(9, 5);
        this.gui = new GuiBuilder(plugin, 5, plugin.getChatManager().message("spectator-gui.teleporter.title")).globalClick(event -> event.setCancelled(true)).pane(pane).build();

        this.registerComponents(pane);
    }

    private void registerComponents(StaticPane pane) {
        var teleporterComponents = new TeleporterComponents();
        teleporterComponents.registerComponents(this, pane);
    }

    public void close() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.user.getPlayer().closeInventory(), 1L);
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
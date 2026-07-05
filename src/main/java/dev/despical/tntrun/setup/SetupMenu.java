/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.setup;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.menu.Menu;
import dev.despical.tntrun.setup.pages.*;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@Getter
public class SetupMenu implements Menu {

    private Gui gui;
    protected PaginatedPane basePane;

    private final User user;
    private final Arena arena;
    private final Map<Integer, Supplier<SetupPage>> pages;

    public SetupMenu(User user, Arena arena) {
        this.user = user;
        this.arena = arena;
        this.pages = new HashMap<>();
        initializeGui();
    }

    private void initializeGui() {
        Main plugin = Main.getInstance();
        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/setup-menu");

        String rawTitle = config.getString("title", "Arena Editor");
        Component title = plugin.getChatManager().parseMessage(rawTitle, Var.of("%arena_id%", arena.getId()));

        gui = new Gui(plugin, 5, title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));

        pages.put(0, () -> new SetupHomePage(this));
        pages.put(1, () -> new LocationsPage(this));
        pages.put(2, () -> new PlayerAmountsPage(this));

        setPage(0);
    }

    public void setPage(int page) {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage setupPage = pages.get(page).get();
        setupPage.beforeOpening(gui);
        setupPage.injectItems(basePane);

        basePane.setPage(0);
        gui.update();
    }

    @Override
    public void open() {
        Player player = user.getPlayer();
        player.setGameMode(GameMode.CREATIVE);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

        gui.show(player);
    }

    public void openPlayerSettings() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage playerSettingsPage = new PlayerSettingsPage(this);
        playerSettingsPage.beforeOpening(gui);
        playerSettingsPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openPotionEffects() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage potionPage = new PotionEffectSelectionPage(this);
        potionPage.beforeOpening(gui);
        potionPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openPotionLevelSelection(String effectName) {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage levelPage = new PotionLevelSelectionPage(this, effectName);
        levelPage.beforeOpening(gui);
        levelPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openSelectedPotionEffects() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage selectedEffectsPage = new SelectedPotionEffectsPage(this);
        selectedEffectsPage.beforeOpening(gui);
        selectedEffectsPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }

    public void openArenaRecordResetConfirmation() {
        gui.removePanes();

        basePane = new PaginatedPane(9, 6);
        gui.addPane(basePane);

        SetupPage confirmationPage = new ResetArenaRecordsConfirmationPage(this);
        confirmationPage.beforeOpening(gui);
        confirmationPage.injectItems(basePane);

        basePane.setPage(0);

        gui.update();
    }
}

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

package dev.despical.tntrun.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.tntrun.music.ArenaMusicSettings;
import dev.despical.tntrun.music.MusicPhase;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Hub of the music editor.
 * <p>
 * It lists one song selector per {@link MusicPhase} and, for the phases that
 * follow another one, a toggle that decides whether an identical song keeps
 * playing or restarts from the beginning.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public class MusicSettingsPage extends SetupPage {

    private static final String BORDER_ITEM_KEY = "music-border";

    private final ArenaMusicSettings settings;

    public MusicSettingsPage(SetupMenu menu) {
        super(menu);
        this.settings = ArenaMusicSettings.of(arena);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(getRows());
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        int rows = getRows();
        StaticPane staticPane = new StaticPane(9, rows);

        paginatedPane.addPane(0, createBorderPane(rows, BORDER_ITEM_KEY));
        paginatedPane.addPane(0, staticPane);

        if (plugin.getRadio().isAvailable()) {
            staticPane.addItem(createSongSelectorItem(MusicPhase.LOBBY), 2, 1);
            staticPane.addItem(createSongSelectorItem(MusicPhase.IN_GAME), 4, 1);
            staticPane.addItem(createSongSelectorItem(MusicPhase.END), 6, 1);

            staticPane.addItem(createContinueToggleItem(MusicPhase.IN_GAME), 4, 2);
            staticPane.addItem(createContinueToggleItem(MusicPhase.END), 6, 2);
        } else {
            staticPane.addItem(createUnavailableItem(), 4, 2);
        }

        staticPane.addItem(createGoBackItem(), 8, rows - 1);
    }

    private int getRows() {
        return plugin.getRadio().isAvailable() ? 4 : 5;
    }

    private GuiItem createSongSelectorItem(MusicPhase phase) {
        SpecialItem specialItem = itemManager.getItem("music-%s-selector".formatted(phase.getId()));
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%selected_song%", settings.getSong(phase).orElse("None"))
        );

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.openSongSelection(phase);
        });
    }

    private GuiItem createContinueToggleItem(MusicPhase phase) {
        String state = settings.isContinuing(phase) ? "enabled" : "disabled";
        SpecialItem specialItem = itemManager.getItem("music-%s-continue-%s".formatted(phase.getId(), state));
        ItemStack item = ItemUtils.formatItemStack(specialItem);

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            settings.toggleContinuing(phase);
            menu.openMusicSettings();
        });
    }

    private GuiItem createUnavailableItem() {
        ItemStack item = itemManager.getItem("song-nbs-not-installed").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        });
    }
}

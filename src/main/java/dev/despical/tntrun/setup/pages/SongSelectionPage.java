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
import dev.despical.tntrun.music.SongPreviewer;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated song picker for one {@link MusicPhase} of an arena.
 * <p>
 * Left click assigns a song, right click previews it, and the first entry
 * switches the phase back to silence.
 *
 * @author Despical
 * <p>
 * Created at 31.10.2026
 */
public class SongSelectionPage extends SetupPage {

    private static final int[][] SONG_SLOTS = {
        {1, 1}, {3, 1}, {5, 1}, {7, 1},
        {1, 3}, {3, 3}, {5, 3}, {7, 3}
    };

    private static final String BORDER_ITEM_KEY = "music-border";

    private final MusicPhase phase;
    private final ArenaMusicSettings settings;
    private final SongPreviewer previewer;

    public SongSelectionPage(SetupMenu menu, MusicPhase phase) {
        super(menu);
        this.phase = phase;
        this.settings = ArenaMusicSettings.of(arena);
        this.previewer = plugin.getRadio().getPreviewer();
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(6);
        gui.setOnClose(event -> previewer.stop((Player) event.getPlayer()));
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        List<GuiItem> songItems = new ArrayList<>();
        songItems.add(createNoSongItem());
        plugin.getRadio().getCatalog().getSongNames().forEach(name -> songItems.add(createSongItem(name)));

        int itemsPerPage = SONG_SLOTS.length;
        int pageCount = Math.max(1, (int) Math.ceil((double) songItems.size() / itemsPerPage));

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            StaticPane staticPane = new StaticPane(9, 6);
            int startIndex = pageIndex * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, songItems.size());

            for (int itemIndex = startIndex; itemIndex < endIndex; itemIndex++) {
                int[] slot = SONG_SLOTS[itemIndex - startIndex];
                staticPane.addItem(songItems.get(itemIndex), slot[0], slot[1]);
            }

            if (pageIndex > 0) {
                staticPane.addItem(createPaginationItem(paginatedPane, pageIndex - 1, "music-previous-page"), 2, 5);
            }

            if (pageIndex < pageCount - 1) {
                staticPane.addItem(createPaginationItem(paginatedPane, pageIndex + 1, "music-next-page"), 6, 5);
            }

            staticPane.addItem(createBackItem(), 4, 5);

            paginatedPane.addPane(pageIndex, createBorderPane(6, BORDER_ITEM_KEY));
            paginatedPane.addPane(pageIndex, staticPane);
        }
    }

    private GuiItem createNoSongItem() {
        SpecialItem specialItem = itemManager.getItem("song-no-selection");
        ItemStack item = format(specialItem, settings.getSong(phase).isEmpty(), Var.of("%phase%", phase.getId()));

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            previewer.stop(player);
            settings.setSong(phase, null);
            menu.openMusicSettings();
        });
    }

    private GuiItem createSongItem(String songName) {
        SpecialItem specialItem = itemManager.getItem("song-item");
        boolean selected = settings.getSong(phase).filter(songName::equals).isPresent();
        ItemStack item = format(specialItem, selected,
            Var.of("%song_name%", songName),
            Var.of("%phase%", phase.getId())
        );

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();

            if (event.getClick() == ClickType.RIGHT) {
                boolean started = previewer.preview(player, songName);

                player.playSound(player.getLocation(), started ? Sound.UI_BUTTON_CLICK : Sound.ENTITY_VILLAGER_NO, 1f, 1.2f);
                return;
            }

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            previewer.stop(player);
            settings.setSong(phase, songName);
            menu.openMusicSettings();
        });
    }

    private GuiItem createBackItem() {
        ItemStack item = itemManager.getItem("music-go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            previewer.stop(player);
            menu.openMusicSettings();
        });
    }

    private GuiItem createPaginationItem(PaginatedPane paginatedPane, int targetPage, String itemKey) {
        ItemStack item = itemManager.getItem(itemKey).getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            previewer.stop(player);
            paginatedPane.setPage(targetPage);
            menu.getGui().update();
        });
    }

    private ItemStack format(SpecialItem specialItem, boolean glowing, Var... vars) {
        ItemStack item = ItemUtils.formatItemStack(specialItem, vars).clone();

        if (glowing) {
            item.editMeta(meta -> {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            });
        }

        return item;
    }
}

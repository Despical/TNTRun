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
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerAmountsPage extends SetupPage {

    public PlayerAmountsPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 4);
        paginatedPane.addPane(0, pane);

        pane.addItem(createAmountItem("min-player-amount", ArenaKeys.MIN_PLAYERS), 3, 1);
        pane.addItem(createAmountItem("max-player-amount", ArenaKeys.MAX_PLAYERS), 5, 1);
        pane.addItem(createGoBackItem(), 8, 3);
    }

    private GuiItem createAmountItem(String itemKey, ArenaOption<Integer> option) {
        SpecialItem specialItem = itemManager.getItem(itemKey);
        ItemStack item = buildAmountItem(specialItem, arena.getOption(option));

        return GuiItem.of(item, event -> {
            ClickType click = event.getClick();
            int amount = arena.getOption(option);
            int originalAmount = amount;

            if (click.isLeftClick()) {
                amount = Math.min(99, amount + 1);
            } else if (click.isRightClick()) {
                amount = Math.max(2, amount - 1);
            }

            if (option == ArenaKeys.MIN_PLAYERS && amount > arena.getOption(ArenaKeys.MAX_PLAYERS)) {
                arena.setOption(ArenaKeys.MAX_PLAYERS, amount);
            } else if (option == ArenaKeys.MAX_PLAYERS && amount < arena.getOption(ArenaKeys.MIN_PLAYERS)) {
                arena.setOption(ArenaKeys.MIN_PLAYERS, amount);
            }

            arena.setOption(option, amount);
            plugin.getSignManager().updateSigns(arena);

            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, amount > originalAmount ? 1.5f : 0.8f);

            menu.setPage(2);
        });
    }

    private ItemStack buildAmountItem(SpecialItem specialItem, int amount) {
        ItemStack item = specialItem.asItemBuilder()
            .amount(Math.clamp(amount, 1, 99))
            .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setMaxStackSize(99);
            item.setItemMeta(meta);
        }

        return item;
    }
}

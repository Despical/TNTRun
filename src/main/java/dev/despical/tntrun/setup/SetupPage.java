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

import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.Pane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.menu.Page;
import dev.despical.tntrun.utils.ItemUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public abstract class SetupPage implements Page {

    protected static final TNTRun plugin = TNTRun.getInstance();

    protected final SetupMenu menu;
    protected final Arena arena;
    protected final ItemManager itemManager;
    protected final ChatManager chatManager;

    protected SetupPage(SetupMenu menu) {
        this.menu = menu;
        this.arena = menu.getArena();
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
    }

    /**
     * Creates a pane that frames the menu with the configured border item.
     * <p>
     * The pane has the lowest priority, so any content item placed on a border
     * slot is drawn on top of it. The look comes from the item registered under
     * {@code itemKey} in the setup menu file, so server owners can change its
     * material or show its tooltip by setting {@code decoration-only} to false.
     *
     * @param rows    the row count of the menu that will show the pane
     * @param itemKey the key of the border item in the setup menu file
     * @return the border pane, which stays empty when the item is not configured
     */
    protected final StaticPane createBorderPane(int rows, String itemKey) {
        StaticPane borderPane = new StaticPane(0, 0, 9, rows, Pane.Priority.LOWEST);
        SpecialItem borderItem = itemManager.getItem(itemKey);

        if (borderItem != null) {
            borderPane.fillBorder(new GuiItem(ItemUtils.formatItemStack(borderItem), event -> event.setCancelled(true)));
        }

        return borderPane;
    }

    protected final GuiItem createGoBackItem() {
        ItemStack item = itemManager.getItem("go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            menu.setPage(0);
        });
    }

    protected final GuiItem createPlayerSettingsBackItem() {
        ItemStack item = itemManager.getItem("go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            menu.openPlayerSettings();
        });
    }

    protected final GuiItem createPotionEffectsBackItem() {
        ItemStack item = itemManager.getItem("go-back").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);

            menu.openPotionEffects();
        });
    }
}

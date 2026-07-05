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
import dev.despical.tntrun.arena.ArenaRecordResetService;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Schedulers;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2026
 */
public class ResetArenaRecordsConfirmationPage extends SetupPage {

    public ResetArenaRecordsConfirmationPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(3);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 3);
        paginatedPane.addPane(0, pane);

        pane.addItem(createRecordPreviewItem(), 4, 0);
        pane.addItem(createConfirmItem(), 2, 1);
        pane.addItem(createCancelItem(), 6, 1);
    }

    private GuiItem createRecordPreviewItem() {
        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%arena_id%", arena.getId()),
            Var.of("%record_holder%", arena.getRecordHolderName()),
            Var.of("%record_time%", Utils.formatTime(arena.getRecordTime()))
        );

        ItemUtils.applyArenaRecordResetHead(item, arena.getRecordHolderName());
        return GuiItem.of(item, event -> event.setCancelled(true));
    }

    private GuiItem createConfirmItem() {
        ItemStack item = itemManager.getItem("confirm-arena-record-reset").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.3f);

            menu.close();

            new ArenaRecordResetService(plugin).resetArenaRecords(arena)
                .whenComplete((_, throwable) -> Schedulers.runInTheNextTick(() -> {
                    if (throwable != null) {
                        plugin.getLogger().log(Level.WARNING, "Failed to reset arena records for " + arena.getId(), throwable);

                        chatManager.sendMessage(player, "setup.arena-record-reset-failed", Var.of("%arena_id%", arena.getId()));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                        return;
                    }

                    chatManager.sendMessage(player, "setup.arena-record-reset", Var.of("%arena_id%", arena.getId()));

                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.7f);
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.3f);
                }));
        });
    }

    private GuiItem createCancelItem() {
        ItemStack item = itemManager.getItem("cancel-arena-record-reset").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);

            menu.setPage(0);
        });
    }
}

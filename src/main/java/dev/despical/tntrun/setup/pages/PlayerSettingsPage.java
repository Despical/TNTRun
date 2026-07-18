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
import dev.despical.tntrun.game.ArenaPotionEffect;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 28.03.2026
 */
public class PlayerSettingsPage extends SetupPage {

    public PlayerSettingsPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane staticPane = new StaticPane(9, 4);
        paginatedPane.addPane(0, staticPane);

        staticPane.addItem(createScoreboardToggleItem(), 1, 1);
        staticPane.addItem(createPotionEffectsSelectorItem(), 3, 1);
        staticPane.addItem(createBossBarToggleItem(), 5, 1);
        staticPane.addItem(createPvpToggleItem(), 7, 1);

        staticPane.addItem(createGoBackItem(), 8, 3);
    }

    private GuiItem createPotionEffectsSelectorItem() {
        SpecialItem specialItem = itemManager.getItem("potion-effects-selector");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            List<ArenaPotionEffect> effects = arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS);
            int effectCount = (effects != null) ? effects.size() : 0;

            Var var = Var.of("%potion_effect_count%", String.valueOf(effectCount));
            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, var))
                    .toList();
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            openPotionEffectsMenu(player);
        });
    }

    private GuiItem createScoreboardToggleItem() {
        SpecialItem specialItem = itemManager.getItem("scoreboard-toggle");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            boolean scoreboardEnabled = arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED);
            String status = scoreboardEnabled ? "<#00E676>ENABLED" : "<#FF5252>DISABLED";

            Var statusVar = Var.of("%scoreboard_toggle_status%", status);

            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, statusVar))
                    .toList();
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            toggleScoreboardOption();
            menu.openPlayerSettings();
        });
    }

    private GuiItem createBossBarToggleItem() {
        SpecialItem specialItem = itemManager.getItem("bossbar-toggle");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            boolean bossBarEnabled = arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED);
            String status = bossBarEnabled ? "<#00E676>ENABLED" : "<#FF5252>DISABLED";

            Var statusVar = Var.of("%bossbar_toggle_status%", status);

            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, statusVar))
                    .toList();
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            toggleBossBarOption();
            menu.openPlayerSettings();
        });
    }

    private GuiItem createPvpToggleItem() {
        SpecialItem specialItem = itemManager.getItem("pvp-toggle");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null && meta.hasLore()) {
            boolean pvpEnabled = arena.getOption(ArenaKeys.ARENA_PVP_ENABLED);
            String status = pvpEnabled ? "<#00E676>ENABLED" : "<#FF5252>DISABLED";

            Var statusVar = Var.of("%pvp_toggle_status%", status);

            List<Component> lore = meta.lore();
            if (lore != null) {
                lore = lore.stream()
                    .map(line -> chatManager.replaceVarsInComponent(line, statusVar))
                    .toList();
                meta.lore(lore);
                item.setItemMeta(meta);
            }
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

            togglePvpOption();
            menu.openPlayerSettings();
        });
    }

    private void openPotionEffectsMenu(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);
        menu.openPotionEffects();
    }

    private void toggleScoreboardOption() {
        boolean newValue = !arena.getOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED);
        arena.setOption(ArenaKeys.ARENA_SCOREBOARD_ENABLED, newValue);

        if (arena.isGameNonnull()) {
            arena.getGame().getScoreboardManager().updateAllScoreboards();
        }
    }

    private void toggleBossBarOption() {
        boolean newValue = !arena.getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED);
        arena.setOption(ArenaKeys.ARENA_BOSSBAR_ENABLED, newValue);

        if (arena.isGameNonnull()) {
            arena.getGame().getBossBarManager().update();
        }
    }

    private void togglePvpOption() {
        boolean newValue = !arena.getOption(ArenaKeys.ARENA_PVP_ENABLED);
        arena.setOption(ArenaKeys.ARENA_PVP_ENABLED, newValue);
    }
}

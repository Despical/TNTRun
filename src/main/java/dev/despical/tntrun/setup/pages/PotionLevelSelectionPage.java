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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 28.03.2026
 */
public class PotionLevelSelectionPage extends SetupPage {

    private final String effectName;

    public PotionLevelSelectionPage(SetupMenu menu, String effectName) {
        super(menu);
        this.effectName = effectName;
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane staticPane = new StaticPane(9, 4);
        paginatedPane.addPane(0, staticPane);

        int slot = 2;

        for (int level = 1; level <= 3; level++) {
            GuiItem levelItem = createLevelItem(level);
            staticPane.addItem(levelItem, slot, 1);
            slot += 2;
        }

        staticPane.addItem(createPotionEffectsBackItem(), 4, 3);
    }

    private GuiItem createLevelItem(int level) {
        SpecialItem specialItem = itemManager.getItem("potion-level-" + level);
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String effectDisplayName = toDisplayName(effectName);
            String romanLevel = toRomanNumeral(level);

            if (meta.hasDisplayName()) {
                Component displayName = meta.displayName();
                if (displayName != null) {
                    displayName = chatManager.replaceVarsInComponent(
                        displayName,
                        Var.of("%effect_name%", effectDisplayName),
                        Var.of("%effect_level%", String.valueOf(level)),
                        Var.of("%effect_level_roman%", romanLevel)
                    );
                    meta.displayName(displayName);
                }
            }

            if (meta.hasLore()) {
                List<Component> lore = meta.lore();
                if (lore != null) {
                    lore = lore.stream()
                        .map(line -> chatManager.replaceVarsInComponent(
                            line,
                            Var.of("%effect_name%", effectDisplayName),
                            Var.of("%effect_level%", String.valueOf(level)),
                            Var.of("%effect_level_roman%", romanLevel)
                        ))
                        .toList();
                    meta.lore(lore);
                }
            }

            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

            List<ArenaPotionEffect> effects = arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS);
            if (effects == null) {
                effects = new ArrayList<>();
            } else {
                effects = new ArrayList<>(effects);
            }

            ArenaPotionEffect newEffect = new ArenaPotionEffect(effectName, level);

            effects.removeIf(e -> e.getEffectType().equalsIgnoreCase(effectName));
            effects.add(newEffect);

            arena.setOption(ArenaKeys.ARENA_POTION_EFFECTS, effects);

            player.sendMessage(Component.text("✓ Added " + toDisplayName(effectName) + " " + toRomanNumeral(level), net.kyori.adventure.text.format.NamedTextColor.GREEN));

            menu.openPotionEffects();
        });
    }

    private String toRomanNumeral(int num) {
        return switch (num) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> String.valueOf(num);
        };
    }

    private String toDisplayName(String value) {
        StringBuilder displayName = new StringBuilder();

        for (String part : value.toLowerCase(Locale.ROOT).split("_")) {
            if (part.isEmpty()) {
                continue;
            }

            if (!displayName.isEmpty()) {
                displayName.append(' ');
            }

            displayName.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                displayName.append(part.substring(1));
            }
        }

        return displayName.toString();
    }
}

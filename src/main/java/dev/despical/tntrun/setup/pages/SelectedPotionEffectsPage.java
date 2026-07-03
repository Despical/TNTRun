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

/**
 * @author Despical
 * <p>
 * Created at 29.03.2026
 */
public class SelectedPotionEffectsPage extends SetupPage {

    public SelectedPotionEffectsPage(SetupMenu menu) {
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

        List<ArenaPotionEffect> effects = arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS);
        if (effects != null) {
            int index = 0;
            for (ArenaPotionEffect effect : effects) {
                if (index >= 14) {
                    break;
                }

                int x = 1 + (index % 7);
                int y = 1 + (index / 7);
                staticPane.addItem(createPotionEffectDisplayItem(effect), x, y);
                index++;
            }
        }

        staticPane.addItem(createPotionEffectsBackItem(), 8, 3);
    }

    private GuiItem createPotionEffectDisplayItem(ArenaPotionEffect effect) {
        SpecialItem specialItem = itemManager.getItem("potion-effect-item");
        ItemStack item = specialItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (meta.hasDisplayName()) {
                Component displayName = meta.displayName();
                if (displayName != null) {
                    displayName = chatManager.replaceVarsInComponent(
                        displayName,
                        Var.of("%effect_name%", effect.getEffectType()),
                        Var.of("%effect_level%", String.valueOf(effect.getLevel()))
                    );
                    meta.displayName(displayName);
                }
            }

            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            if (!event.isRightClick()) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.8f);

            List<ArenaPotionEffect> effects = arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS);
            if (effects == null) {
                return;
            }

            effects = new ArrayList<>(effects);
            effects.remove(effect);
            arena.setOption(ArenaKeys.ARENA_POTION_EFFECTS, effects);
            menu.openSelectedPotionEffects();
        });
    }
}

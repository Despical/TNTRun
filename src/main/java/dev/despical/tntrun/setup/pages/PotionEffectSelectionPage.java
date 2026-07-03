package dev.despical.tntrun.setup.pages;

import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
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
public class PotionEffectSelectionPage extends SetupPage {

    private static final int CONTENT_COLUMNS = 7;
    private static final int MIN_ROWS = 4;
    private static final int MAX_ROWS = 6;

    public PotionEffectSelectionPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(getDynamicRows(getAvailableEffects().size()));
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        List<String> effectList = new ArrayList<>(getAvailableEffects());
        int rows = getDynamicRows(effectList.size());
        int itemsPerPage = getItemsPerPage(rows);
        int controlsRow = rows - 1;
        int pageCount = Math.max(1, (int) Math.ceil((double) effectList.size() / itemsPerPage));

        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            StaticPane staticPane = new StaticPane(9, rows);

            int startIndex = pageIndex * itemsPerPage;
            int endIndex = Math.min(startIndex + itemsPerPage, effectList.size());

            for (int itemIndex = startIndex; itemIndex < endIndex; itemIndex++) {
                GuiItem effectItem = createEffectItem(effectList.get(itemIndex));
                int relativeIndex = itemIndex - startIndex;
                int x = 1 + (relativeIndex % CONTENT_COLUMNS);
                int y = 1 + (relativeIndex / CONTENT_COLUMNS);

                staticPane.addItem(effectItem, x, y);
            }

            if (pageIndex > 0) {
                staticPane.addItem(createPaginationItem(paginatedPane, pageIndex - 1, false), 0, controlsRow);
            }

            if (pageIndex < pageCount - 1) {
                staticPane.addItem(createPaginationItem(paginatedPane, pageIndex + 1, true), 7, controlsRow);
            }

            staticPane.addItem(createSelectedEffectsItem(), 4, controlsRow);
            staticPane.addItem(createPlayerSettingsBackItem(), 8, controlsRow);
            paginatedPane.addPane(pageIndex, staticPane);
        }
    }

    private List<String> getAvailableEffects() {
        List<String> effects = itemManager.getItem("add-potion-effect").getCustomKey("available-effects");
        return effects != null ? effects : List.of();
    }

    private int getDynamicRows(int effectCount) {
        int visibleEffects = Math.min(effectCount, CONTENT_COLUMNS * (MAX_ROWS - 2));
        int contentRows = Math.max(1, (int) Math.ceil((double) visibleEffects / CONTENT_COLUMNS));

        return Math.max(MIN_ROWS, Math.min(MAX_ROWS, contentRows + 2));
    }

    private int getItemsPerPage(int totalRows) {
        return CONTENT_COLUMNS * (totalRows - 2);
    }

    private GuiItem createEffectItem(String effectName) {
        String itemKey = "potion-effect-" + effectName.toLowerCase(Locale.ROOT).replace('_', '-');
        SpecialItem specialItem = itemManager.getItem(itemKey);
        ItemStack item = specialItem != null
            ? specialItem.getItemStack()
            : itemManager.getItem("add-potion-effect").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.5f);

            menu.openPotionLevelSelection(effectName);
        });
    }

    private GuiItem createPaginationItem(PaginatedPane paginatedPane, int targetPage, boolean nextPage) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(nextPage ? "Next Page" : "Previous Page", NamedTextColor.GOLD));
            item.setItemMeta(meta);
        }

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            paginatedPane.setPage(targetPage);
            menu.getGui().update();
        });
    }

    private GuiItem createSelectedEffectsItem() {
        SpecialItem specialItem = itemManager.getItem("selected-potion-effects");
        ItemStack item = specialItem != null
            ? specialItem.getItemStack()
            : new ItemStack(Material.ENDER_CHEST);

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            List<?> selectedEffects = arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS);

            if (selectedEffects == null || selectedEffects.isEmpty()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                player.sendMessage(Component.text("No potion effects have been selected yet.", NamedTextColor.RED));
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.1f);

            menu.openSelectedPotionEffects();
        });
    }
}

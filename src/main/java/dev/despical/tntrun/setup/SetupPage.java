package dev.despical.tntrun.setup;

import dev.despical.fileitems.ItemManager;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.menu.Page;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class SetupPage implements Page {

    protected static final Main plugin = Main.getInstance();

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

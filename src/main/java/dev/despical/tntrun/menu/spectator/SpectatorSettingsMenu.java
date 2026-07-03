package dev.despical.tntrun.menu.spectator;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.menu.Menu;
import dev.despical.tntrun.stats.StatisticType;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2026
 */
public class SpectatorSettingsMenu implements Menu {

    private static final int MAX_SPEED_LEVEL = 3;
    private static final Main plugin = Main.getInstance();

    private final ItemManager itemManager;
    private final ChatManager chatManager;
    private final User viewer;
    private final Game game;
    private final Gui gui;

    public SpectatorSettingsMenu(User viewer) {
        this.itemManager = plugin.getItemManager();
        this.chatManager = plugin.getChatManager();
        this.viewer = viewer;
        this.game = viewer.getArena().getGame();
        this.gui = createGui();
    }

    @Override
    public Gui getGui() {
        return gui;
    }

    @Override
    public void open() {
        Player player = viewer.getPlayer();
        if (player == null) {
            return;
        }

        draw();
        gui.show(player);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    private Gui createGui() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/spectator-settings-menu");
        Component title = chatManager.parseMessage(config.getString("title", ""));

        Gui gui = new Gui(plugin, config.getInt("rows", 5), title);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnClose(_ -> plugin.getDatabase().saveData(viewer));
        return gui;
    }

    private void draw() {
        gui.removePanes();

        StaticPane pane = new StaticPane(0, 0, 9, gui.getRows());
        addDecorations(pane);

        addToggleItem(pane, "night-vision", Statistics.SPECTATOR_NIGHT_VISION_LEVEL, this::toggleNightVision);
        addToggleItem(pane, "show-spectators", Statistics.SPECTATOR_SHOW_OTHERS, this::toggleSpectators);

        addSpeedItem(pane);
        addCloseItem(pane);

        gui.addPane(pane);
        gui.update();
    }

    private void addDecorations(StaticPane pane) {
        for (SpecialItem item : itemManager.getItemsFromCategory("spectator-settings-menu-items")) {
            if (!Boolean.TRUE.equals(item.getCustomKey("decoration-only"))) {
                continue;
            }

            placeItem(pane, item, new GuiItem(ItemUtils.formatItemStack(item), event -> event.setCancelled(true)));
        }
    }

    private void addToggleItem(StaticPane pane, String key, StatisticType<Integer> stat, Runnable action) {
        String itemKey = key + (viewer.getStatistic(stat) == 1 ? "-enabled" : "-disabled");
        SpecialItem item = itemManager.getItemFromCategory("spectator-settings-menu-items", itemKey);
        if (item == null) {
            return;
        }

        placeItem(pane, item, new GuiItem(ItemUtils.formatItemStack(item), event -> {
            event.setCancelled(true);

            action.run();
            draw();

            playToggleSound(viewer.getStatistic(stat) == 1);
        }));
    }

    private void addSpeedItem(StaticPane pane) {
        int speed = Math.clamp(viewer.getStatistic(Statistics.SPECTATOR_SPEED), 0, MAX_SPEED_LEVEL);
        SpecialItem item = itemManager.getItemFromCategory("spectator-settings-menu-items", speed == 0 ? "speed-disabled" : "speed");

        if (item == null) {
            return;
        }

        ItemStack stack = ItemUtils.formatItemStack(item, Var.of("%speed_level%", speed));
        if (speed > 0) {
            stack.setAmount(speed);
        }

        placeItem(pane, item, new GuiItem(stack, event -> {
            event.setCancelled(true);

            int nextSpeed = (speed + 1) % (MAX_SPEED_LEVEL + 1);
            viewer.setStatistic(Statistics.SPECTATOR_SPEED, nextSpeed);

            game.applySpectatorSettings(viewer);

            draw();
            playSpeedSound(nextSpeed);
        }));
    }

    private void addCloseItem(StaticPane pane) {
        SpecialItem item = itemManager.getItemFromCategory("spectator-settings-menu-items", "close");
        if (item == null) {
            return;
        }

        placeItem(pane, item, new GuiItem(ItemUtils.formatItemStack(item), event -> {
            event.setCancelled(true);
            playClickSound();
            viewer.closeOpenedInventory();
        }));
    }

    private void toggleNightVision() {
        int value = viewer.getStatistic(Statistics.SPECTATOR_NIGHT_VISION_LEVEL) == 1 ? 0 : 1;
        viewer.setStatistic(Statistics.SPECTATOR_NIGHT_VISION_LEVEL, value);
        game.applySpectatorSettings(viewer);
    }

    private void toggleSpectators() {
        int value = viewer.getStatistic(Statistics.SPECTATOR_SHOW_OTHERS) == 1 ? 0 : 1;
        viewer.setStatistic(Statistics.SPECTATOR_SHOW_OTHERS, value);
        game.updateSpectatorVisibility();
    }

    private void playClickSound() {
        Player player = viewer.getPlayer();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
    }

    private void playToggleSound(boolean enabled) {
        Player player = viewer.getPlayer();
        if (player == null) {
            return;
        }

        Sound sound = enabled ? Sound.BLOCK_NOTE_BLOCK_PLING : Sound.BLOCK_NOTE_BLOCK_BIT;
        float pitch = enabled ? 1.65f : .55f;

        player.playSound(player.getLocation(), sound, 1f, pitch);
    }

    private void playSpeedSound(int speed) {
        Player player = viewer.getPlayer();
        if (player == null) {
            return;
        }

        if (speed == 0) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1f, .55f);
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, .8f + speed * .35f);
    }

    private void placeItem(StaticPane pane, SpecialItem specialItem, GuiItem guiItem) {
        List<Integer> slots = specialItem.getCustomKey("slots");

        if (slots != null) {
            slots.forEach(slot -> addAtSlot(pane, guiItem, slot));
            return;
        }

        Integer slot = specialItem.getCustomKey("slot");
        if (slot != null) {
            addAtSlot(pane, guiItem, slot);
        }
    }

    private void addAtSlot(StaticPane pane, GuiItem guiItem, int slot) {
        if (slot < 0 || slot >= gui.getRows() * 9) {
            return;
        }

        pane.addItem(guiItem, slot % 9, slot / 9);
    }
}

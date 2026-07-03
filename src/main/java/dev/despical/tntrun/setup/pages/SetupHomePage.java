package dev.despical.tntrun.setup.pages;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.sign.SignManager;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class SetupHomePage extends SetupPage {

    private final StaticPane pane;

    public SetupHomePage(SetupMenu menu) {
        super(menu);
        this.pane = new StaticPane(9, 5);
    }

    @Override
    public void beforeOpening(Gui gui) {
        int rows = arena.getOption(ArenaKeys.READY) ? 3 : 4;
        gui.setRows(rows);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        pane.addItem(createArenaLocationsItem(), 1, 1);
        pane.addItem(createPlayerAmountsItem(), 3, 1);
        pane.addItem(createArenaSignItem(), 5, 1);
        pane.addItem(createPlayerSettingsItem(), 7, 1);

        if (!arena.getOption(ArenaKeys.READY)) {
            pane.addItem(createRegisterItem(), 8, 3);
        }

        paginatedPane.addPane(0, pane);
    }

    private GuiItem createArenaLocationsItem() {
        ItemStack item = itemManager.getItem("game-locations").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.setPage(1);
        });
    }

    private GuiItem createPlayerAmountsItem() {
        ItemStack item = itemManager.getItem("player-amounts").getItemStack();

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.setPage(2);
        });
    }

    private GuiItem createArenaSignItem() {
        ItemStack item = itemManager.getItem("arena-sign").getItemStack();
        Consumer<InventoryClickEvent> consumer = event -> {
            menu.close();

            Player player = (Player) event.getWhoClicked();
            Block block = player.getTargetBlock(null, 10);

            if (!(block.getState() instanceof Sign)) {
                chatManager.sendMessage(player, "sign.look-at-a-sign");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            SignManager signManager = plugin.getSignManager();
            signManager.addArenaSign(arena, block);

            chatManager.sendMessage(player, "sign.created", signManager.getSignVars(block));

            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2.0f);
            player.playSound(player.getLocation(), Sound.BLOCK_WOOD_PLACE, 1f, 0.8f);
        };

        return GuiItem.of(item, consumer);
    }

    private GuiItem createPlayerSettingsItem() {
        ItemStack item = itemManager.getItem("player-settings").getItemStack();

        Consumer<InventoryClickEvent> consumer = event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.openPlayerSettings();
        };

        return GuiItem.of(item, consumer);
    }

    private GuiItem createRegisterItem() {
        SpecialItem specialItem = itemManager.getItem("register-arena");
        ItemStack item = specialItem.getItemStack();

        Consumer<InventoryClickEvent> eventConsumer = event -> {
            menu.close();

            Player player = (Player) event.getWhoClicked();
            String missingInfo = null;

            if (arena.getOption(ArenaKeys.LOBBY_LOCATION) == null) {
                missingInfo = "Lobby Location";
            } else if (arena.getOption(ArenaKeys.START_LOCATION) == null) {
                missingInfo = "Start Location";
            } else if (arena.getOption(ArenaKeys.END_LOCATION) == null) {
                missingInfo = "End Location";
            } else if (arena.getOption(ArenaKeys.MIN_PLAYERS) > arena.getOption(ArenaKeys.MAX_PLAYERS)) {
                missingInfo = "Player Amounts";
            }

            if (missingInfo != null) {
                List<String> errorMessages = specialItem.getCustomKey("missing-option");

                chatManager.sendCenteredMessage(player, errorMessages, Var.of("%option%", missingInfo));
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 0.5f);
                return;
            }

            arena.setOption(ArenaKeys.READY, true);
            arena.start();

            Var[] vars = {
                Var.of("%arena_id%", arena.getId()),
                Var.of("%command%", "/tr join " + arena.getId())
            };

            List<String> messages = specialItem.getCustomKey("registered-successfully");
            messages = messages.stream().map(line -> Utils.format(line, vars)).toList();

            chatManager.sendCenteredMessage(player, messages);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        };

        return GuiItem.of(item, eventConsumer);
    }
}

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

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.tntrun.sign.SignManager;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Schedulers;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public class SetupHomePage extends SetupPage {

    private static final String MAP_NAME_INPUT_KEY = "map_name";
    private static final String MAP_AUTHOR_INPUT_KEY = "map_author";
    private static final String MAP_DIFFICULTY_INPUT_KEY = "map_difficulty";

    private final StaticPane pane;

    public SetupHomePage(SetupMenu menu) {
        super(menu);
        this.pane = new StaticPane(9, 5);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(5);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        pane.addItem(createArenaLocationsItem(), 1, 1);
        pane.addItem(createPlayerAmountsItem(), 3, 1);
        pane.addItem(createArenaSignItem(), 5, 1);
        pane.addItem(createPlayerSettingsItem(), 7, 1);
        pane.addItem(createMapNameItem(), 1, 3);

        GuiItem resetArenaRecordsItem = createArenaRecordResetItem();
        if (resetArenaRecordsItem != null) {
            pane.addItem(resetArenaRecordsItem, 3, 3);
        }

        if (!arena.getOption(ArenaKeys.READY)) {
            pane.addItem(createRegisterItem(), 8, 4);
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
            SignManager signManager = plugin.getSignManager();

            if (!(block.getState() instanceof Sign)) {
                signManager.sendMessage(player, "look-at-a-sign");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            signManager.addArenaSign(arena, block);

            signManager.sendMessage(player, "created", signManager.getSignVars(block));

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

    private GuiItem createMapNameItem() {
        SpecialItem specialItem = itemManager.getItem("map-name");
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%map_name%", arena.getOption(ArenaKeys.MAP_NAME)),
            Var.of("%map_author%", arena.getOption(ArenaKeys.MAP_AUTHOR)),
            Var.of("%map_difficulty%", arena.getOption(ArenaKeys.MAP_DIFFICULTY))
        );

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);

            menu.close();

            Schedulers.runInTheNextTick(() -> player.showDialog(createMapNameDialog()));
        });
    }

    private Dialog createMapNameDialog() {
        String currentMapName = arena.getOption(ArenaKeys.MAP_NAME);
        String currentMapAuthor = arena.getOption(ArenaKeys.MAP_AUTHOR);
        String currentMapDifficulty = arena.getOption(ArenaKeys.MAP_DIFFICULTY);

        return Dialog.create(factory -> factory.empty()
            .base(DialogBase.builder(chatManager.parseMessage("<#00E676><bold>Map Details"))
                .externalTitle(chatManager.parseMessage("<#00E676>Map Details"))
                .canCloseWithEscape(true)
                .pause(false)
                .afterAction(DialogBase.DialogAfterAction.CLOSE)
                .inputs(List.of(
                    DialogInput.text(MAP_NAME_INPUT_KEY, chatManager.parseMessage("<#B0BEC5>Map name"))
                        .initial(currentMapName)
                        .maxLength(48)
                        .width(220)
                        .build(),
                    DialogInput.text(MAP_AUTHOR_INPUT_KEY, chatManager.parseMessage("<#B0BEC5>Map author"))
                        .initial(currentMapAuthor)
                        .maxLength(48)
                        .width(220)
                        .build(),
                    DialogInput.text(MAP_DIFFICULTY_INPUT_KEY, chatManager.parseMessage("<#B0BEC5>Map difficulty"))
                        .initial(currentMapDifficulty)
                        .maxLength(48)
                        .width(220)
                        .build()
                ))
                .build())
            .type(DialogType.notice(ActionButton.builder(chatManager.parseMessage("<#00E676><bold>Save"))
                .tooltip(chatManager.parseMessage("<#B0BEC5>Save this arena's map details."))
                .width(150)
                .action(DialogAction.customClick(this::handleMapNameSubmit, ClickCallback.Options.builder()
                    .uses(1)
                    .lifetime(Duration.ofMinutes(5))
                    .build()))
                .build()))
        );
    }

    private void handleMapNameSubmit(DialogResponseView response, Audience audience) {
        if (!(audience instanceof Player player)) {
            return;
        }

        String mapName = response.getText(MAP_NAME_INPUT_KEY);
        if (mapName == null || mapName.isBlank()) {
            chatManager.sendRawMessage(player, "<#FF5252>✖ <#BDBDBD>Map name cannot be empty.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String mapAuthor = response.getText(MAP_AUTHOR_INPUT_KEY);
        if (mapAuthor == null || mapAuthor.isBlank()) {
            chatManager.sendRawMessage(player, "<#FF5252>✖ <#BDBDBD>Map author cannot be empty.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        String mapDifficulty = response.getText(MAP_DIFFICULTY_INPUT_KEY);
        if (mapDifficulty == null || mapDifficulty.isBlank()) {
            chatManager.sendRawMessage(player, "<#FF5252>✖ <#BDBDBD>Map difficulty cannot be empty.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        mapName = mapName.trim();
        mapAuthor = mapAuthor.trim();
        mapDifficulty = mapDifficulty.trim();

        arena.setOption(ArenaKeys.MAP_NAME, mapName);
        arena.setOption(ArenaKeys.MAP_AUTHOR, mapAuthor);
        arena.setOption(ArenaKeys.MAP_DIFFICULTY, mapDifficulty);

        Var[] vars = {
            Var.of("%map_name%", mapName),
            Var.of("%map_author%", mapAuthor),
            Var.of("%map_difficulty%", mapDifficulty)
        };
        chatManager.sendRawMessage(player, "<#00E676>✔ <#BDBDBD>Map details set to <#29B6F6>%map_name% <#BDBDBD>by <#29B6F6>%map_author% <#BDBDBD>(<green>%map_difficulty%<#BDBDBD>).", vars);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.7f);
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

    private GuiItem createArenaRecordResetItem() {
        String recordHolderName = arena.getRecordHolderName();
        long recordTime = arena.getRecordTime();

        if (recordTime <= 0 || recordHolderName == null || recordHolderName.equalsIgnoreCase("None")) {
            return null;
        }

        SpecialItem specialItem = itemManager.getItem("arena-record-reset");
        ItemStack item = ItemUtils.formatItemStack(specialItem,
            Var.of("%record_holder%", recordHolderName),
            Var.of("%record_time%", Utils.formatTime(recordTime))
        );
        ItemUtils.applyArenaRecordResetHead(item, recordHolderName);

        return GuiItem.of(item, event -> {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.15f);

            menu.openArenaRecordResetConfirmation();
        });
    }
}

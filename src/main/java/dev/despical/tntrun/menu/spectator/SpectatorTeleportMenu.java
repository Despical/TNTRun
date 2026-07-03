package dev.despical.tntrun.menu.spectator;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.fileitems.ItemManager;
import dev.despical.fileitems.SpecialItem;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.menu.Menu;
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
public class SpectatorTeleportMenu implements Menu {

    private static final Main plugin = Main.getInstance();

    private final ItemManager itemManager;
    private final ChatManager chatManager;
    private final User viewer;
    private final Game game;
    private final Gui gui;
    private int page;

    public SpectatorTeleportMenu(User viewer) {
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
        FileConfiguration config = ConfigUtils.getConfig(plugin, "menu/spectator-teleporter-menu");
        Component title = chatManager.parseMessage(config.getString("title", ""));

        Gui baseGui = new Gui(plugin, config.getInt("rows", 5), title);
        baseGui.setOnGlobalClick(event -> event.setCancelled(true));
        return baseGui;
    }

    private void draw() {
        gui.removePanes();

        StaticPane background = new StaticPane(0, 0, 9, gui.getRows());
        addDecorations(background);

        PaginatedPane players = new PaginatedPane(1, 1, 7, Math.max(1, gui.getRows() - 2));
        players.populateWithGuiItems(createTeleportItems());
        players.setPage(Math.clamp(players.getPages() - 1, 0, page));

        StaticPane navigation = new StaticPane(0, 0, 9, gui.getRows());
        addNavigationItems(navigation, players);

        gui.addPane(background);
        gui.addPane(players);
        gui.addPane(navigation);
        gui.update();
    }

    private void addDecorations(StaticPane pane) {
        for (SpecialItem item : itemManager.getItemsFromCategory("spectator-teleporter-menu-items")) {
            if (!Boolean.TRUE.equals(item.getCustomKey("decoration-only"))) {
                continue;
            }

            placeItem(pane, item, new GuiItem(ItemUtils.formatItemStack(item), event -> event.setCancelled(true)));
        }
    }

    private void addNavigationItems(StaticPane pane, PaginatedPane players) {
        if (players.getPages() <= 1) {
            return;
        }

        int currentPage = players.getPage();

        if (currentPage > 0) {
            SpecialItem previous = itemManager.getItemFromCategory("spectator-teleporter-menu-items", "previous-page");
            if (previous != null) {
                placeItem(pane, previous, new GuiItem(ItemUtils.formatItemStack(previous), event -> {
                    event.setCancelled(true);

                    page = currentPage - 1;

                    draw();
                    playClickSound();
                }));
            }
        }

        if (currentPage < players.getPages() - 1) {
            SpecialItem next = itemManager.getItemFromCategory("spectator-teleporter-menu-items", "next-page");
            if (next != null) {
                placeItem(pane, next, new GuiItem(ItemUtils.formatItemStack(next), event -> {
                    event.setCancelled(true);

                    page = currentPage + 1;

                    draw();
                    playClickSound();
                }));
            }
        }
    }

    private List<GuiItem> createTeleportItems() {
        SpecialItem template = itemManager.getItemFromCategory("spectator-teleporter-menu-items", "teleport-player");
        if (template == null) {
            return List.of();
        }

        Arena arena = game.getArena();
        return game.getPlayersLeft().stream()
            .filter(user -> !arena.isDeathPlayer(user))
            .filter(user -> user.getPlayer() != null)
            .map(target -> createTeleportItem(template, target))
            .toList();
    }

    private GuiItem createTeleportItem(SpecialItem template, User target) {
        Player targetPlayer = target.getPlayer();
        ItemStack item = ItemUtils.formatItemStack(template, Var.of("%target%", target.getName()));
        ItemUtils.applyPlayerProfileIfSkull(targetPlayer, item);

        return new GuiItem(item, event -> {
            event.setCancelled(true);

            Player player = viewer.getPlayer();
            if (player == null || targetPlayer == null || !targetPlayer.isOnline() || target.isSpectator()) {
                draw();
                return;
            }

            player.teleport(targetPlayer.getLocation());
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1.4f);
        });
    }

    private void playClickSound() {
        Player player = viewer.getPlayer();
        if (player != null) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        }
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

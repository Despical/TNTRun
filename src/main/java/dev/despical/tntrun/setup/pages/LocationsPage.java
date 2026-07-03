package dev.despical.tntrun.setup.pages;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.setup.SetupMenu;
import dev.despical.tntrun.setup.SetupPage;
import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.GuiItem;
import dev.despical.inventoryframework.pane.PaginatedPane;
import dev.despical.inventoryframework.pane.StaticPane;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.function.Consumer;

public class LocationsPage extends SetupPage {

    public LocationsPage(SetupMenu menu) {
        super(menu);
    }

    @Override
    public void beforeOpening(Gui gui) {
        gui.setRows(4);
    }

    @Override
    public void injectItems(PaginatedPane paginatedPane) {
        StaticPane pane = new StaticPane(9, 4);
        paginatedPane.addPane(0, pane);

        int x = 0;
        List<ArenaOption<Location>> keys = List.of(ArenaKeys.LOBBY_LOCATION, ArenaKeys.START_LOCATION, ArenaKeys.END_LOCATION);

        for (ArenaOption<Location> key : keys) {
            pane.addItem(createGuiItemFor(key), x += 2, 1);
        }

        pane.addItem(createGoBackItem(), 8, 3);
    }

    private GuiItem createGuiItemFor(ArenaOption<Location> option) {
        var item = itemManager.getItem(option.getKey());

        Consumer<InventoryClickEvent> eventConsumer = event -> {
            Player player = (Player) event.getWhoClicked();
            Location location = player.getLocation();

            player.playSound(location, Sound.BLOCK_LODESTONE_PLACE, 1f, 1f);
            chatManager.sendRawMessage(player, item.getCustomKey("message"));

            Location targetLocation = event.isShiftClick()
                ? location.getBlock().getLocation().add(0.5, 0, 0.5)
                : location.clone();

            targetLocation.setYaw(location.getYaw());
            targetLocation.setPitch(0);

            arena.setOption(option, targetLocation);

            menu.close();
        };

        return GuiItem.of(item.getItemStack(), eventConsumer);
    }
}

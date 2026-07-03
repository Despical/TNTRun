package dev.despical.tntrun.menu;

import dev.despical.inventoryframework.Gui;
import dev.despical.inventoryframework.pane.PaginatedPane;

/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
public interface Page {

    void beforeOpening(Gui gui);

    void injectItems(PaginatedPane paginatedPane);
}

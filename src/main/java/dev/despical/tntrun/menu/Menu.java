package dev.despical.tntrun.menu;

import dev.despical.inventoryframework.Gui;

/**
 * @author Despical
 * <p>
 * Created at 9.12.2025
 */
public interface Menu {

    Gui getGui();

    void open();

    default void close() {
        getGui().close();
    }
}

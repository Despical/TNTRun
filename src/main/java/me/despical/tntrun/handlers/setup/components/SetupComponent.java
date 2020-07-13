package me.despical.tntrun.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import me.despical.tntrun.handlers.setup.SetupInventory;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public interface SetupComponent {

	void prepare(SetupInventory setupInventory);

	void injectComponents(StaticPane pane);
}
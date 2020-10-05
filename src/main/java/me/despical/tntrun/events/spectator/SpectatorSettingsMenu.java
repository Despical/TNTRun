package me.despical.tntrun.events.spectator;

import com.github.despical.inventoryframework.Gui;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.events.spectator.components.MiscComponents;
import me.despical.tntrun.events.spectator.components.SpeedComponents;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SpectatorSettingsMenu implements Listener {

	private final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final Player player;
	private Gui gui;

	public SpectatorSettingsMenu(Player player) {
		this.player = player;

		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 4, plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Inventory-Name"));
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));

		StaticPane pane = new StaticPane(9, 4);
		this.gui.addPane(pane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		SpeedComponents speedComponents = new SpeedComponents();
		speedComponents.prepare(this);
		speedComponents.injectComponents(pane);

		MiscComponents miscComponents = new MiscComponents();
		miscComponents.prepare(this);
		miscComponents.injectComponents(pane);
	}

	public void openInventory() {
		gui.show(player);
	}

	public Main getPlugin() {
		return plugin;
	}

	public Player getPlayer() {
		return player;
	}
}
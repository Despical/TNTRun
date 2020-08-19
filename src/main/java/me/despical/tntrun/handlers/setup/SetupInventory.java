package me.despical.tntrun.handlers.setup;

import java.util.Random;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.stefvanschie.inventoryframework.Gui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.components.ArenaRegisterComponent;
import me.despical.tntrun.handlers.setup.components.MiscComponents;
import me.despical.tntrun.handlers.setup.components.PlayerAmountComponents;
import me.despical.tntrun.handlers.setup.components.SpawnComponents;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SetupInventory {

	private static final Random random = new Random();
	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private final FileConfiguration config = ConfigUtils.getConfig(plugin, "arenas");
	private final Arena arena;
	private final Player player;
	private Gui gui;
	private final SetupUtilities setupUtilities;

	public SetupInventory(Arena arena, Player player) {
		this.arena = arena;
		this.player = player;
		this.setupUtilities = new SetupUtilities(config, arena);
		prepareGui();
	}

	private void prepareGui() {
		this.gui = new Gui(plugin, 1, "TNT Run Arena Editor");
		this.gui.setOnGlobalClick(e -> e.setCancelled(true));
		StaticPane pane = new StaticPane(9, 4);
		this.gui.addPane(pane);
		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.prepare(this);
		spawnComponents.injectComponents(pane);

		PlayerAmountComponents playerAmountComponents = new PlayerAmountComponents();
		playerAmountComponents.prepare(this);
		playerAmountComponents.injectComponents(pane);

		MiscComponents miscComponents = new MiscComponents();
		miscComponents.prepare(this);
		miscComponents.injectComponents(pane);

		ArenaRegisterComponent arenaRegisterComponent = new ArenaRegisterComponent();
		arenaRegisterComponent.prepare(this);
		arenaRegisterComponent.injectComponents(pane);
	}

	private void sendProTip(Player p) {
		int rand = random.nextInt(7 + 1);
		switch (rand) {
		case 0:
			p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Despical/TNTRun"));
			break;
		case 1:
			p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Join our discord server: https://discordapp.com/invite/Vhyy4HA"));
			break;
		case 2:
			p.sendMessage(plugin.getChatManager().colorRawMessage("&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/TNTRun/wiki"));
			break;
		default:
			break;
		}
	}

	public void openInventory() {
		sendProTip(player);
		gui.show(player);
	}

	public Main getPlugin() {
		return plugin;
	}

	public FileConfiguration getConfig() {
		return config;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}

	public Gui getGui() {
		return gui;
	}

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}
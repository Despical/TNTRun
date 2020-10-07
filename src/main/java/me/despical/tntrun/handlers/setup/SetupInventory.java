/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

package me.despical.tntrun.handlers.setup;

import java.util.Random;

import me.despical.tntrun.handlers.ChatManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.despical.inventoryframework.Gui;
import com.github.despical.inventoryframework.pane.StaticPane;

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
		StaticPane pane = new StaticPane(9, 1);
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
		ChatManager chatManager = plugin.getChatManager();
		int rand = random.nextInt(16 + 1);

		switch (rand) {
			case 0:
				p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7We are open source! You can always help us by contributing! Check https://github.com/Despical/TNTRun"));
				break;
			case 1:
				p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Need help? Join our discord server: https://discordapp.com/invite/Vhyy4HA"));
				break;
			case 2:
				p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/TNTRun/wiki"));
				break;
			case 3:
				p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Want to access exclusive maps, addons and more? Check our Patreon page: https://www.patreon.com/despical"));
				break;
			case 4:
				p.sendMessage(chatManager.colorRawMessage("&e&lTIP: &7Help us translating plugin to your language here: https://github.com/Despical/LocaleStorage/"));
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

	public SetupUtilities getSetupUtilities() {
		return setupUtilities;
	}
}
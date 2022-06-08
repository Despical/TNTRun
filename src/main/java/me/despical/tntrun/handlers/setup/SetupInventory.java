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

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.inventoryframework.Gui;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.ChatManager;
import me.despical.tntrun.handlers.setup.components.ArenaRegisterComponent;
import me.despical.tntrun.handlers.setup.components.MiscComponents;
import me.despical.tntrun.handlers.setup.components.PlayerAmountComponents;
import me.despical.tntrun.handlers.setup.components.SpawnComponents;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class SetupInventory {

	private final Main plugin;
	private final Arena arena;
	private final Player player;
	private final FileConfiguration config;
	private final SetupUtilities setupUtilities;

	private Gui gui;

	public SetupInventory(Main plugin, Arena arena, Player player) {
		this.plugin = plugin;
		this.arena = arena;
		this.player = player;
		this.config = ConfigUtils.getConfig(plugin, "arenas");
		this.setupUtilities = new SetupUtilities(config);

		prepareGui();
	}

	private void prepareGui() {
		gui = new Gui(plugin, 5, "TNT Run Arena Editor");
		gui.setOnGlobalClick(event -> event.setCancelled(true));

		StaticPane pane = new StaticPane(9, 5);
		pane.fillProgressBorder(GuiItem.of(XMaterial.GREEN_STAINED_GLASS_PANE.parseItem()), GuiItem.of(XMaterial.BLACK_STAINED_GLASS_PANE.parseItem()), arena.isReady() ? 100 : 0);

		gui.addPane(pane);

		prepareComponents(pane);
	}

	private void prepareComponents(StaticPane pane) {
		SpawnComponents spawnComponents = new SpawnComponents();
		spawnComponents.registerComponent(this, pane);

		PlayerAmountComponents playerAmountComponents = new PlayerAmountComponents();
		playerAmountComponents.registerComponent(this, pane);

		MiscComponents miscComponents = new MiscComponents();
		miscComponents.registerComponent(this, pane);

		ArenaRegisterComponent arenaRegisterComponent = new ArenaRegisterComponent();
		arenaRegisterComponent.registerComponent(this, pane);
	}

	private void sendTip(Player p) {
		ChatManager chatManager = plugin.getChatManager();

		switch (ThreadLocalRandom.current().nextInt(12 + 1)) {
			case 0:
				p.sendMessage(chatManager.color("&e&lTIP: &7Need help? Join our discord server: https://discordapp.com/invite/Vhyy4HA"));
				break;
			case 1:
				p.sendMessage(chatManager.color("&e&lTIP: &7Need help? Check our wiki: https://github.com/Despical/TNTRun/wiki"));
				break;
			case 2:
				p.sendMessage(chatManager.color("&e&lTIP: &7Still thinking donating? Check our Patreon page: https://www.patreon.com/despical"));
				break;
			case 3:
				p.sendMessage(chatManager.color("&e&lTIP: &7Help us translating plugin to your language here: https://github.com/Despical/LocaleStorage/"));
				break;
			default:
				break;
		}
	}

	public void openInventory() {
		sendTip(player);
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
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
import me.despical.commons.item.ItemBuilder;
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
	private final SetupUtilities setupUtilities;

	private Gui gui;

	public SetupInventory(Main plugin, Arena arena, Player player) {
		this.plugin = plugin;
		this.arena = arena;
		this.player = player;
		this.setupUtilities = new SetupUtilities(plugin, arena.getId());

		prepareGui();
	}

	private void prepareGui() {
		gui = new Gui(plugin, 5, "TNT Run Arena Editor");
		gui.setOnGlobalClick(event -> event.setCancelled(true));

		StaticPane pane = new StaticPane(9, 5);
		ItemBuilder registeredItem = new ItemBuilder(XMaterial.GREEN_STAINED_GLASS_PANE).name("&aArena Validation Successful"),
			notRegisteredItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena Validation Not Finished Yet");
		pane.fillProgressBorder(GuiItem.of(registeredItem.build()), GuiItem.of(notRegisteredItem.build()), arena.isReady() ? 100 : 0);
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

	private void sendTip() {
		ChatManager chatManager = plugin.getChatManager();
		String tip = "";

		switch (ThreadLocalRandom.current().nextInt(12)) {
			case 0:
				tip = "We are open source! You can always help us by contributing! Check https://github.com/Despical/TNTRun";
				break;
			case 1:
				tip = "Need help? Check our wiki: https://github.com/Despical/TNTRun/wiki";
				break;
			case 2:
				tip = "Help us translating plugin to your language here: https://github.com/Despical/LocaleStorage/";
				break;
			case 3:
				tip = "You can support us with becoming Patron on https://www.patreon.com/despical to make updates better and sooner.";
				break;
			case 4:
				tip = "Need help? You can join our Discord community. Check out https://discord.gg/rVkaGmyszE";
				break;
			case 5:
				tip = "You have suggestions to improve the plugin? Use our issue tracker or join our Discord server.";
				break;
			default:
				break;
		}

		if (!tip.isEmpty()) {
			player.sendMessage(chatManager.color("&e&lTIP: &7" + tip));
		}
	}

	public void openInventory() {
		sendTip();
		gui.show(player);
	}

	public Main getPlugin() {
		return plugin;
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
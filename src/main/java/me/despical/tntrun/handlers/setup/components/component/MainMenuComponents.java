/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.tntrun.handlers.setup.components.component;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.Strings;
import me.despical.commons.util.conversation.ConversationBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import me.despical.tntrun.handlers.setup.components.AbstractComponent;
import org.bukkit.block.Sign;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 * Recoded at 21.05.2023
 */
public class MainMenuComponents extends AbstractComponent {

	public MainMenuComponents(ArenaEditorGUI gui) {
		super(gui);
	}

	@Override
	public void registerComponents(PaginatedPane paginatedPane) {
		final var pane = new StaticPane(9, 4);
		final var config = ConfigUtils.getConfig(plugin, "arena");
		final var readyItem = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE).name("&aArena is registered properly!");
		final var notReadyItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena configuration is not validated yet!");
		final var lobbyLocationsItem = new ItemBuilder(XMaterial.WHITE_CONCRETE).name("    &e&lSet Lobby/End Locations").lore("&7Click to set start and end locations.").lore("", "&7Lobby Location: " + isOptionDoneBool("lobbyLocation", config), "&7End Location:    " + isOptionDoneBool("endLocation", config));
		final var playerAmountsItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST).name("   &e&lSet Min/Max Players").lore(" &7Click to set player amounts.").lore("", "&a&l✔ &7Minimum  Players Amount: &8" + arena.getMinimumPlayers()).lore("&a&l✔ &7Maximum Players Amount: &8" + arena.getMaximumPlayers()).glow();
		final var mapNameItem = new ItemBuilder(XMaterial.NAME_TAG).name("    &e&lSet Map Name").lore("&7Click to set map name.").lore("", "&7Currently: " + arena.getMapName()).glow();

		if (isOptionDoneBoolean("lobbyLocation", config) && isOptionDoneBoolean("endLocation", config)) {
			lobbyLocationsItem.glow();
		}

		pane.fillWith(arena.isReady() ? readyItem.build() : notReadyItem.build());
		pane.fillProgressBorder(GuiItem.of(readyItem.build()), GuiItem.of(notReadyItem.build()), arena.getSetupProgress());
		pane.addItem(GuiItem.of(lobbyLocationsItem.build(), event -> this.gui.setPage("   Set LOBBY and END locations", 3, 1)), 1, 1);
		pane.addItem(GuiItem.of(playerAmountsItem.build(), event -> this.gui.setPage(" Set MIN and MAX player amount", 3, 3)), 7, 1);

		pane.addItem(GuiItem.of(mapNameItem.build(), event -> {
			user.closeOpenedInventory();

			new ConversationBuilder(plugin).withPrompt(new StringPrompt() {

				@Override
				@NotNull
				public String getPromptText(@NotNull ConversationContext context) {
					return Strings.format("&ePlease type the map name of arena in the chat. You can use color codes.");
				}

				@Override
				public Prompt acceptInput(@NotNull ConversationContext context, String input) {
					var name = Strings.format(input);

					arena.setMapName(name);

					config.set(path + "mapName", name);
					ConfigUtils.saveConfig(plugin, config, "arena");

					plugin.getServer().getScheduler().runTask(plugin, () -> user.sendRawMessage("&e✔ Completed | &aMap name of arena &e{0} &aset to &e{1}&a.", arena.getId(), name));
					return Prompt.END_OF_CONVERSATION;
				}
			}).buildFor(user.getPlayer());
		}), 3, 1);

		var gameSignItem = new ItemBuilder(XMaterial.OAK_SIGN).name("&e&l      Add Game Sign");

		if (!plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
			gameSignItem.lore("&7Target a sign and click this.");
		} else {
			gameSignItem
				.lore("&cThis option disabled in Bungee-cord mode.", "")
				.lore("&8Bungee mode is meant to be one arena per server.")
				.lore("&8If you wish to have multi arena, disable bungee in config!");
		}

		pane.addItem(GuiItem.of(gameSignItem.build(), e -> {
			if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) return;

			user.closeOpenedInventory();

			var block = user.getPlayer().getTargetBlock(null, 10);

			if (!(block.getState() instanceof Sign)) {
				user.sendRawMessage("&cYou are not looking at any sign block!");
				return;
			}

			final var signManager = plugin.getSignManager();

			if (signManager.isGameSign(block)) {
				user.sendRawMessage("&cThis sign is already a game sign!");
				return;
			}

			final var locations = config.getStringList(path + "signs");
			locations.add(LocationSerializer.toString(block.getLocation()));

			config.set(path + "signs", locations);
			ConfigUtils.saveConfig(plugin, config, "arena");

			signManager.addArenaSign(block, arena);
			signManager.updateSign(arena);

			user.sendRawMessage("&aArena sign has been created successfully!");
		}), 5, 1);

		ItemStack registerItem;

		if (arena.isReady()) {
			registerItem = new ItemBuilder(XMaterial.BARRIER)
				.name("&a&l           Arena Registered")
				.lore("&7Good job, you went through whole setup!")
				.lore("&7      You can play on this arena now!")
				.glow()
				.build();
		} else {
			registerItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("       &e&lFinish Arena Setup")
				.lore("&7  Click this when you are done.")
				.lore("&7You'll still be able to edit arena.")
				.flag(ItemFlag.HIDE_POTION_EFFECTS)
				.build();
		}

		pane.addItem(GuiItem.of(registerItem, e -> {
			user.closeOpenedInventory();

			if (arena.isReady()) {
				user.sendRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!");
				return;
			}

			final String[] locations = {"lobbyLocation", "endLocation"};

			for (final var location : locations) {
				if (!config.isSet(path + location) || LocationSerializer.isDefaultLocation(config.getString(path + location))) {
					user.sendRawMessage("&c&l✘ Arena validation failed! Please configure following spawn properly: {0} (cannot be world spawn location)", location);
					return;
				}
			}

			user.sendRawMessage("&a&l✔ Validation succeeded! Registering new arena instance: &e&l{0}", arena.getId());

			config.set(path + "ready", true);
			ConfigUtils.saveConfig(plugin, config, "arena");

			arena.setReady(true);
			arena.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
			arena.setMapName(config.getString(path + "mapName"));
			arena.setMinimumPlayers(config.getInt(path + "minimumPlayers"));
			arena.setMaximumPlayers(config.getInt(path + "maximumPlayers"));
			arena.setLobbyLocation(LocationSerializer.fromString(config.getString(path + "lobbyLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.start();
		}), 8, 3);

		paginatedPane.addPane(0, pane);
	}
}
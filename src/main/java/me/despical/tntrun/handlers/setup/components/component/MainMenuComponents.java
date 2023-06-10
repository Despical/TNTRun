/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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

import de.rapha149.signgui.SignGUI;
import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.PaginatedPane;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.handlers.setup.ArenaEditorGUI;
import me.despical.tntrun.handlers.setup.components.AbstractComponent;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

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
		final var mapNameItem = new ItemBuilder(XMaterial.NAME_TAG).name("&e&lSet Map Name").lore("&7Click to set arena map name.").lore("", "&7Currently: " + arena.getMapName()).enchantment(Enchantment.ARROW_INFINITE).flag(ItemFlag.HIDE_ENCHANTS);
		final var readyItem = new ItemBuilder(XMaterial.LIME_STAINED_GLASS_PANE).name("&aArena is registered properly!");
		final var notReadyItem = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).name("&cArena configuration is not validated yet!");
		final var lobbyLocationsItem = new ItemBuilder(XMaterial.WHITE_CONCRETE).name("&e&lSet Lobby/End Locations").lore("&7Click to set lobby and ending locations.").lore("", "&7Lobby Location: " + isOptionDoneBool("lobbyLocation"), "&7End Location:    " + isOptionDoneBool("endLocation"));
		final var playerAmountsItem = new ItemBuilder(XMaterial.GLOWSTONE_DUST).name("&e&lSet Min/Max Players").lore("&7Click to set player amounts.").lore("", "&a&l✔ &7Minimum  Players Amount: &8" + arena.getMinimumPlayers()).lore("&a&l✔ &7Maximum Players Amount: &8" + arena.getMaximumPlayers()).enchantment(Enchantment.ARROW_INFINITE).flag(ItemFlag.HIDE_ENCHANTS);

		if (isOptionDoneBoolean("lobbyLocation") && isOptionDoneBoolean("endLocation")) lobbyLocationsItem.enchantment(Enchantment.ARROW_INFINITE).flag(ItemFlag.HIDE_ENCHANTS);

		pane.fillWith(arena.isReady() ? readyItem.build() : notReadyItem.build());
		pane.fillProgressBorder(GuiItem.of(readyItem.build()), GuiItem.of(notReadyItem.build()), arena.getSetupProgress());
		pane.addItem(GuiItem.of(lobbyLocationsItem.build(), event -> this.gui.setPage("   Set LOBBY and END locations", 3, 1)), 1, 1);
		pane.addItem(GuiItem.of(playerAmountsItem.build(), event -> this.gui.setPage(" Set MIN and MAX player amount", 3, 3)), 7, 1);

		pane.addItem(GuiItem.of(mapNameItem.build(), event -> {
			user.closeOpenedInventory();

			new SignGUI()
				.lines("Type name in 2. line", "", "", "Click done to set")
				.type(Material.DARK_OAK_SIGN)
				.color(DyeColor.LIGHT_GRAY)
				.onFinish((p, lines) -> {

					if (!lines[1].isEmpty() && !lines[3].isEmpty()) {
						final var name = lines[1];

						arena.setMapName(name);

						config.set(path + "mapName", name);
						saveConfig();

						user.sendRawMessage("&e✔ Completed | &aName of arena &e%s &aset to &e%s", arena, name);
						return null;
					} else {
						user.sendRawMessage("&c✘ Not Completed | &aName of arena can not be empty!");
						return lines;
					}
				}).open(user.getPlayer());
		}), 3, 1);

		ItemStack registerItem;

		if (arena.isReady()) {
			registerItem = new ItemBuilder(XMaterial.BARRIER)
				.name("&a&lArena Registered - Congratulations")
				.lore("&7This arena is already registered!")
				.lore("&7Good job, you went through whole setup!")
				.lore("&7You can play on this arena now!")
				.enchantment(Enchantment.DURABILITY)
				.flag(ItemFlag.HIDE_ENCHANTS)
				.build();
		} else {
			registerItem = new ItemBuilder(XMaterial.FIREWORK_ROCKET)
				.name("&e&lFinish Setup")
				.lore("&7Click this when you're done with configuration.")
				.lore("&7It will validate and register the arena.")
				.build();
		}

		pane.addItem(GuiItem.of(registerItem, e -> {
			user.closeOpenedInventory();

			if (arena.isReady()) {
				user.sendRawMessage("&a&l✔ &aThis arena was already validated and is ready to use!");
				return;
			}

			final String[] locations = {"lobbyLocation", "endLocation"}, spawns = {"playerSpawnPoints", "goldSpawnPoints"};

			for (final var location : locations) {
				if (!config.isSet(path + location) || LocationSerializer.isDefaultLocation(config.getString(path + location))) {
					user.sendRawMessage("&c&l✘ &cArena validation failed! Please configure following spawn properly: %s (cannot be world spawn location)", location);
					return;
				}
			}

			for (final var spawn : spawns) {
				if (!config.isSet(path + spawn) || config.getStringList(path + spawn).size() < arena.getMaximumPlayers()) {
					user.sendRawMessage("&c&l✘ &cArena validation failed! Please configure following spawns properly: %s (must be minimum %d spawns)", spawn, arena.getMaximumPlayers());
					return;
				}
			}

			user.sendRawMessage("&a&l✔ &aValidation succeeded! Registering new arena instance: &e%s", arena);

			config.set(path + "ready", true);
			saveConfig();

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
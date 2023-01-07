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

package me.despical.tntrun.handlers.setup.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.serializer.LocationSerializer;
import me.despical.commons.util.conversation.ConversationBuilder;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.setup.SetupInventory;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class MiscComponents implements SetupComponent {

	@Override
	public void registerComponent(SetupInventory setupInventory, StaticPane pane) {
		Player player = setupInventory.getPlayer();
		Arena arena = setupInventory.getArena();
		ItemStack signItem = new ItemBuilder(XMaterial.OAK_SIGN)
				.name("&e&lAdd Game Sign")
				.lore("&7Target a sign and click this.")
				.lore("&8(this will set target sign as game sign)")
				.build();

		pane.addItem(GuiItem.of(signItem, e -> {
			player.closeInventory();
			Block block = player.getTargetBlock(null, 10);

			if (!(block.getState() instanceof Sign)) {
				player.sendMessage(chatManager.prefixedMessage("Commands.Look-Sign"));
				return;
			}

			if (block.getLocation().distance(player.getWorld().getSpawnLocation()) <= plugin.getServer().getSpawnRadius() && e.getClick() != ClickType.SHIFT_LEFT) {
				player.sendMessage(chatManager.color("&c&l✖ &cWarning | Server spawn protection is set to &6" + plugin.getServer().getSpawnRadius() + "&c and sign you want to place is in radius of this protection! &c&lPlayers with no perm won't be able to interact with this sign and can't join the game so."));
				player.sendMessage(chatManager.color("&cYou can ignore this warning and add sign with Shift + Left Click, but for now&c&l operation is cancelled!"));
				return;
			}

			plugin.getSignManager().addArenaSign(block, arena);

			player.sendMessage(chatManager.prefixedMessage("Signs.Sign-Created"));

			List<String> locations = config.getStringList("instances." + arena.getId() + ".signs");
			locations.add(LocationSerializer.toString(block.getLocation()));

			config.set("instances." + arena.getId() + ".signs", locations);
			saveConfig();
		}), 5, 1);

		pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.NAME_TAG)
			.name("&e&lSet Map Name")
			.lore("&7Click to set arena map name")
			.lore("", "&a&lCurrently: &e" + config.getString("instances." + arena.getId() + ".mapName"))
			.build(), e -> {

			player.closeInventory();

			new ConversationBuilder(plugin).withPrompt(new StringPrompt() {

				@Override
				@NotNull
				public String getPromptText(@NotNull ConversationContext context) {
					return chatManager.prefixedRawMessage("&ePlease type in chat arena name! You can use color codes.");
				}

				@Override
				public Prompt acceptInput(@NotNull ConversationContext context, String input) {
					String name = chatManager.color(input);
					arena.setMapName(name);

					player.sendMessage(chatManager.color("&e✔ Completed | &aName of arena " + arena.getId() + " set to " + name));

					config.set("instances." + arena.getId() + ".mapName", arena.getMapName());
					saveConfig();

					new SetupInventory(plugin, arena, player).openInventory();
					return Prompt.END_OF_CONVERSATION;
				}
			}).buildFor(player);
		}), 6, 1);

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.FILLED_MAP)
			.name("&e&lView Wiki Page")
			.lore("&7Having problems with setup or wanna")
			.lore("&7know some useful tips? Click to get wiki link!")
			.build(), e -> {

			player.closeInventory();
			player.sendMessage(chatManager.prefixedRawMessage("&7Check out our wiki: https://github.com/Despical/TNTRun/wiki"));
		}), 6, 3);
	}
}

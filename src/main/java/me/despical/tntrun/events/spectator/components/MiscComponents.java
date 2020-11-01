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

package me.despical.tntrun.events.spectator.components;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.events.spectator.SpectatorSettingsMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Despical
 * <p>
 * Created at 05.10.2020
 */
public class MiscComponents implements SpectatorSettingComponent {

	private SpectatorSettingsMenu spectatorSettingsMenu;

	@Override
	public void prepare(SpectatorSettingsMenu spectatorSettingsMenu) {
		this.spectatorSettingsMenu = spectatorSettingsMenu;
	}

	@Override
	public void injectComponents(StaticPane pane) {
		Main plugin = spectatorSettingsMenu.getPlugin();
		Player player = spectatorSettingsMenu.getPlayer();
		Arena arena = ArenaRegistry.getArena(player);
		ItemStack nightVision;

		if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
			nightVision = new ItemBuilder(Material.ENDER_PEARL)
				.name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Disable-Night-Vision"))
				.lore(plugin.getChatManager().getStringList("In-Game.Spectator.Settings-Menu.Disable-Night-Vision-Lore"))
				.build();
		} else {
			nightVision = new ItemBuilder(Material.ENDER_EYE)
				.name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Enable-Night-Vision"))
				.lore(plugin.getChatManager().getStringList("In-Game.Spectator.Settings-Menu.Enable-Night-Vision-Lore"))
				.build();
		}

		pane.addItem(new GuiItem(nightVision, e -> {
			e.getWhoClicked().closeInventory();

			if (player.hasPotionEffect(PotionEffectType.NIGHT_VISION)) {
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			} else {
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0));
			}
		}), 2, 2);

		boolean canSee = arena.getPlayers().stream().filter(p -> plugin.getUserManager().getUser(p).isSpectator()).anyMatch(player::canSee);
		ItemStack specItem;

		if (canSee) {
			specItem = new ItemBuilder(Material.REDSTONE)
				.name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Hide-Spectators"))
				.lore(plugin.getChatManager().getStringList("In-Game.Spectator.Settings-Menu.Hide-Spectators-Lore"))
				.build();
		} else {
			specItem = new ItemBuilder(Material.GLOWSTONE_DUST)
				.name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Show-Spectators"))
				.lore(plugin.getChatManager().getStringList("In-Game.Spectator.Settings-Menu.Show-Spectators-Lore"))
				.build();
		}

		pane.addItem(new GuiItem(specItem, e -> {
			e.getWhoClicked().closeInventory();
			if (canSee) {
				arena.getPlayers().stream().filter(p -> plugin.getUserManager().getUser(p).isSpectator()).forEach(p -> player.hidePlayer(plugin, p));
				player.sendMessage(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Show-Spectators-Message"));
			} else {
				arena.getPlayers().stream().filter(p -> plugin.getUserManager().getUser(p).isSpectator()).forEach(p -> player.showPlayer(plugin, p));
				player.sendMessage(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Hide-Spectators-Message"));
			}
		}), 3, 2);
	}
}
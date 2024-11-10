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

package me.despical.tntrun.events.spectator.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.compat.XPotion;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.miscellaneous.PlayerUtils;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.events.spectator.SpectatorSettingsGUI;
import me.despical.tntrun.user.User;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 10.02.2023
 */
public class SettingComponents {

    private Main plugin;

    public void registerComponents(SpectatorSettingsGUI spectatorGui, StaticPane pane) {
        this.plugin = spectatorGui.getPlugin();

        var chatManager = plugin.getChatManager();
        var prefix = chatManager.message("spectator-gui.speed-prefix");
        var user = spectatorGui.getUser();
        var arena = spectatorGui.getArena();
        var player = spectatorGui.getUser().getPlayer();

        pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.LEATHER_BOOTS).name(chatManager.message("spectator-gui.no-speed")).hideTooltip().build(), e -> setSpeed(user, -1, "0")), 2, 1);
        pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.CHAINMAIL_BOOTS).name(prefix + "I").hideTooltip().build(), e -> setSpeed(user, 1, "I")), 3, 1);
        pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.IRON_BOOTS).name(prefix + "II").hideTooltip().build(), e -> setSpeed(user, 2, "II")), 4, 1);
        pane.addItem(GuiItem.of(new ItemBuilder(XMaterial.GOLDEN_BOOTS).name(prefix + "III").hideTooltip().build(), e -> setSpeed(user, 3, "III")), 5, 1);
        pane.addItem(GuiItem.of(new ItemBuilder(Material.DIAMOND_BOOTS).name(prefix + "IV").hideTooltip().build(), e -> setSpeed(user, 4, "IV")), 6, 1);

        var hasNightVision = player.hasPotionEffect(XPotion.NIGHT_VISION.getPotionEffectType());
        var shouldHaveNightVision = user.getStat(StatisticType.SPECTATOR_NIGHT_VISION) == 1;

        pane.addItem(this.buildItem("night-vision-item", shouldHaveNightVision ? XMaterial.ENDER_EYE : XMaterial.ENDER_PEARL, hasNightVision, event -> {
            if (hasNightVision) {
                player.removePotionEffect(XPotion.NIGHT_VISION.getPotionEffectType());
            } else {
                player.addPotionEffect(XPotion.NIGHT_VISION.buildInvisible(Integer.MAX_VALUE, 3));
            }

            user.setStat(StatisticType.SPECTATOR_NIGHT_VISION, !hasNightVision ? 0 : 1);
            user.sendMessage("messages.spectators." + (hasNightVision ? "removed-night-vision" : "have-night-vision"));

            plugin.getUserManager().saveStatistic(user, StatisticType.SPECTATOR_NIGHT_VISION);

            new SpectatorSettingsGUI(plugin, user, arena).showGui();
        }), 2, 2);

        var seeOthers = user.getStat(StatisticType.SPECTATOR_SHOW_OTHERS) == 1;

        pane.addItem(this.buildItem("hide-spectators-item", seeOthers ? XMaterial.REDSTONE : XMaterial.GLOWSTONE_DUST, seeOthers, event -> {
            for (var u : arena.getPlayers()) {
                if (!u.isSpectator()) continue;

                if (seeOthers) {
                    PlayerUtils.hidePlayer(player, u.getPlayer(), plugin);
                } else {
                    PlayerUtils.showPlayer(player, u.getPlayer(), plugin);
                }
            }

            user.setStat(StatisticType.SPECTATOR_SHOW_OTHERS, seeOthers ? 0 : 1);
            user.sendMessage("messages.spectators." + (seeOthers ? "hide-spectators" : "show-spectators"));

            plugin.getUserManager().saveStatistic(user, StatisticType.SPECTATOR_SHOW_OTHERS);

            new SpectatorSettingsGUI(plugin, user, arena).showGui();
        }), 3, 2);
    }

    private void setSpeed(User user, int level, String prefix) {
        var speed = .1F + (level + 1) * .05F;
        var player = user.getPlayer();

        player.closeInventory();
        player.setFlySpeed(speed);
        player.removePotionEffect(XPotion.SPEED.getPotionEffectType());

        if (level != -1) {
            player.addPotionEffect(XPotion.SPEED.buildInvisible(Integer.MAX_VALUE, level));
        }

        user.setStat(StatisticType.SPECTATOR_SPEED, level);
        user.sendRawMessage(plugin.getChatManager().message("messages.spectators." + (level == -1 ? "no-longer-speed" : "have-speed")).replace("%speed%", prefix));
    }

    private GuiItem buildItem(String itemName, XMaterial enabledItem, boolean condition, Consumer<InventoryClickEvent> event) {
        var chatManager = plugin.getChatManager();
        var path = "spectator-gui.%s.".formatted(itemName);

        return new GuiItem(new ItemBuilder(enabledItem)
            .name(chatManager.message(path + (condition ? "disable" : "enable")))
            .lore(chatManager.getStringList(path + (condition ? "disabled-lore" : "enabled-lore")))
            .build(), event);
    }
}

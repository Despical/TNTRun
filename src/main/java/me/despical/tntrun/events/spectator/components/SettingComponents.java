package me.despical.tntrun.events.spectator.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.miscellaneous.PlayerUtils;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.events.spectator.SpectatorSettingsGUI;
import me.despical.tntrun.user.User;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 10.02.2023
 */
public class SettingComponents implements SpectatorSettingsGUI.SpectatorSettingComponent {

	private Main plugin;

	@Override
	public void registerComponents(SpectatorSettingsGUI spectatorGui, StaticPane pane) {
		this.plugin = spectatorGui.getPlugin();

		var prefix = "&aSpeed ";
		var user = spectatorGui.getUser();
		var arena = spectatorGui.getArena();
		var player = spectatorGui.getUser().getPlayer();

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.LEATHER_BOOTS).name("&aNo Speed").flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> setSpeed(user, -1, "0")),2,1);
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.CHAINMAIL_BOOTS).name(prefix + "I").flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> setSpeed(user, 1, "I")),3,1);
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.IRON_BOOTS).name(prefix + "II").flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> setSpeed(user, 2, "II")),4,1);
		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.GOLDEN_BOOTS).name(prefix + "III").flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> setSpeed(user, 3, "III")),5,1);
		pane.addItem(new GuiItem(new ItemBuilder(Material.DIAMOND_BOOTS).name(prefix + "IV").flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> setSpeed(user, 4, "IV")),6,1);

		var hasNightVision = player.hasPotionEffect(PotionEffectType.NIGHT_VISION);
		var shouldHaveNightVision = user.getStat(StatsStorage.StatisticType.SPECTATOR_NIGHT_VISION) == 1;

		pane.addItem(this.buildItem("night-vision-item", shouldHaveNightVision ? XMaterial.ENDER_EYE : XMaterial.ENDER_PEARL, hasNightVision, event -> {
			if (hasNightVision) {
				player.removePotionEffect(PotionEffectType.NIGHT_VISION);
			} else {
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 2, false, false, false));
			}

			user.setStat(StatsStorage.StatisticType.SPECTATOR_NIGHT_VISION, !hasNightVision ? 0 : 1);
			user.sendMessage("messages.spectators." + (hasNightVision ? "removed-night-vision" : "have-night-vision"));

			plugin.getUserManager().saveStatistic(user, StatsStorage.StatisticType.SPECTATOR_NIGHT_VISION);

			new SpectatorSettingsGUI(plugin, user, arena).showGui();
		}), 2, 2);

		var seeOthers = user.getStat(StatsStorage.StatisticType.SPECTATOR_SHOW_OTHERS) == 1;

		pane.addItem(this.buildItem("hide-spectators-item", seeOthers ? XMaterial.REDSTONE : XMaterial.GLOWSTONE_DUST, seeOthers, event -> {
			for (final var u : arena.getPlayers()) {
				if (!u.isSpectator()) continue;

				if (seeOthers) {
					PlayerUtils.hidePlayer(player , u.getPlayer(), plugin);
				} else {
					PlayerUtils.showPlayer(player , u.getPlayer(), plugin);
				}
			}

			user.setStat(StatsStorage.StatisticType.SPECTATOR_SHOW_OTHERS, seeOthers ? 0 : 1);
			user.sendMessage("messages.spectators." + (seeOthers ? "hide-spectators" : "show-spectators"));

			plugin.getUserManager().saveStatistic(user, StatsStorage.StatisticType.SPECTATOR_SHOW_OTHERS);

			new SpectatorSettingsGUI(plugin, user, arena).showGui();
		}), 3, 2);
	}

	private void setSpeed(final User user, final int level, final String prefix) {
		final var speed = .1F + (level + 1) * .05F;
		final var player = user.getPlayer();

		player.closeInventory();
		player.setFlySpeed(speed);
		player.removePotionEffect(PotionEffectType.SPEED);

		if (level != -1) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, level, false, false, false));
		}

		user.setStat(StatsStorage.StatisticType.SPECTATOR_SPEED, level);
		user.sendRawMessage(plugin.getChatManager().message("messages.spectators." + (level == -1 ? "no-longer-speed" : "have-speed")).replace("%speed%", prefix));
	}

	private GuiItem buildItem(final String itemName, final XMaterial enabledItem, final boolean condition, final Consumer<InventoryClickEvent> event) {
		final var chatManager = plugin.getChatManager();
		final var path = "spectator-gui.%s.".formatted(itemName);

		return new GuiItem(new ItemBuilder(enabledItem)
			.name(chatManager.message(path + (condition ? "disable" : "enable")))
			.lore(chatManager.getStringList(path + (condition ? "disabled-lore" : "enabled-lore")))
			.build(), event);
	}
}
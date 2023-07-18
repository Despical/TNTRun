package me.despical.tntrun.events.spectator.components;

import me.despical.commons.compat.XMaterial;
import me.despical.commons.item.ItemBuilder;
import me.despical.commons.item.ItemUtils;
import me.despical.inventoryframework.GuiItem;
import me.despical.inventoryframework.pane.StaticPane;
import me.despical.inventoryframework.util.GeometryUtil;
import me.despical.tntrun.events.spectator.SpectatorTeleporterGUI;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class TeleporterComponents {

	private final static List<Integer> headPlaces = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);

	public void registerComponents(SpectatorTeleporterGUI teleporterGui, StaticPane pane) {
		var u = teleporterGui.getUser();
		var p = u.getPlayer();
		var arena = teleporterGui.getArena();
		var chatManager = teleporterGui.getPlugin().getChatManager();

		pane.addItem(new GuiItem(new ItemBuilder(XMaterial.BARRIER).name(chatManager.message("spectator-gui.close-item")).flag(ItemFlag.HIDE_ATTRIBUTES).build(), e -> e.getWhoClicked().closeInventory()),4,4);

		var players = new ArrayList<>(arena.getPlayersLeft());
		players.remove(u);

		for (int i = 0; i < players.size(); i++) {
			var user = players.get(i);
			var player = user.getPlayer();
			var skullItem = ItemUtils.PLAYER_HEAD_ITEM.clone();
			var skullMeta = (SkullMeta) skullItem.getItemMeta();

			ItemUtils.setPlayerHead(player, skullMeta);
			skullItem.setItemMeta(skullMeta);

			var guiItem = new ItemBuilder(skullItem)
				.name(chatManager.message("spectator-gui.teleporter.skull-name").replace("%player%", player.getName()))
				.lore(chatManager.getStringList("spectator-gui.teleporter.lore")).build();
			var xy = GeometryUtil.slotToXY(headPlaces.get(i));
			int x = xy[0][0], y = xy[0][1];

			pane.addItem(new GuiItem(guiItem, e -> p.teleport(player)), x, y);
		}
	}
}
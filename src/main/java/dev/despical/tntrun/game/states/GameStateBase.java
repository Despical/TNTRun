/*
 * TNT Run - A fast-paced parkour minigame for Minecraft.
 * Copyright (C) 2026  Berke Akçen
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

package dev.despical.tntrun.game.states;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.fileitems.ItemManager;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaOption;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.visibility.VisibilityManager;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
abstract sealed class GameStateBase permits GameStateHandler {

    protected static final Main plugin = Main.getInstance();
    protected static final ItemManager itemManager = plugin.getItemManager();
    protected static final ChatManager chatManager = plugin.getChatManager();

    protected final Game game;
    protected final Arena arena;
    protected final VisibilityManager visibilityManager;

    public GameStateBase(Game game) {
        this.game = game;
        this.arena = game.getArena();
        this.visibilityManager = game.getVisibilityManager();
    }

    public final Location getLocation(ArenaOption<Location> location) {
        return arena.getOption(location);
    }

    public final void handleLevelBarTimer(int timer) {
        if (BooleanOption.LEVEL_BAR_TIMER.value()) {
            game.getPlayers().forEach(player -> player.setLevel(timer));
        }
    }

    public void resetPlayerAttributes(Player player) {
        Utils.resetPlayerAttributes(player);
    }

    public final void saveInventory(Player player) {
        InventorySerializer.saveInventoryToFile(plugin, player);
    }

    public final void saveAndClearInventory(Player player) {
        saveInventory(player);
        clearInventory(player);
    }

    public final void clearAndLoadInventory(Player player) {
        clearInventory(player);

        InventorySerializer.loadInventory(plugin, player);
    }

    public final void clearInventory(Player player) {
        var inventory = player.getInventory();
        inventory.clear();
        inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);
    }
}

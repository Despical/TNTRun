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

package dev.despical.tntrun.sign;

import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.event.ListenerAdapter;
import dev.despical.tntrun.user.User;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
@RequiredArgsConstructor
public class ArenaSignEvents extends ListenerAdapter {

    private final SignManager signManager;

    @EventHandler
    public void onSignDestroy(BlockBreakEvent event) {
        Block block = event.getBlock();
        ArenaSign arenaSign = signManager.getArenaSignByBlock(block);

        if (arenaSign == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission("tntrun.sign.break")) {
            event.setCancelled(true);

            signManager.sendMessage(player, "no-perm-to-break");
            return;
        }

        signManager.removeArenaSign(arenaSign);
        signManager.sendMessage(player, "removed", signManager.getSignVars(block));
    }

    @EventHandler
    public void onJoinAttempt(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        ArenaSign arenaSign = signManager.getArenaSignByBlock(block);
        if (arenaSign == null) {
            return;
        }

        event.setCancelled(true);

        Arena arena = arenaSign.arena();
        if (arena == null) {
            return;
        }

        User user = userManager.getUser(event.getPlayer());
        arenaManager.joinAttempt(user, arena);
    }
}

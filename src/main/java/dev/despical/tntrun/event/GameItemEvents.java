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

package dev.despical.tntrun.event;

import dev.despical.fileitems.SpecialItem;
import dev.despical.tntrun.api.event.player.PlayerLeaveGameEvent;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 21.05.2023
 */
public class GameItemEvents extends ListenerAdapter {

    private final Set<UUID> leaveConfirmations = new HashSet<>();

	@EventHandler
	public void onDoubleJump(PlayerToggleFlightEvent event) {
		var player = event.getPlayer();

		if (!event.isFlying() && player.getGameMode() != GameMode.ADVENTURE) {
			return;
		}

		User user = plugin.getUserManager().getUser(player);
		Arena arena = user.getArena();

		if (arena == null || user.isSpectator() || arena.isDeathPlayer(user)) {
			return;
		}

        event.setCancelled(performDoubleJump(user, player, arena));
	}

	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Action action = event.getAction();

		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK || action == Action.PHYSICAL) return;

		User user = plugin.getUserManager().getUser(player);

		if (!user.isInArena()) {
			return;
		}

		if (event.getItem() == null) {
			return;
		}

		var doubleJumpItem = plugin.getItemManager().getItem("double-jump");

		if (doubleJumpItem == null) {
			return;
		}

		if (doubleJumpItem.equals(event.getItem())) {
            Arena arena = user.getArena();
            if (arena == null || user.isSpectator() || arena.isDeathPlayer(user)) {
                return;
            }

			event.setCancelled(performDoubleJump(user, player, arena));
		}
	}

    private boolean performDoubleJump(User user, Player player, Arena arena) {
        if (user.getCooldown("double_jump") > 0) {
            player.setFlying(false);
            return true;
        }

        int jumps = user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS);
        if (jumps <= 0) {
            player.setAllowFlight(false);
            player.setFlying(false);
            return true;
        }

        user.setStatistic(Statistics.LOCAL_DOUBLE_JUMPS, jumps - 1);
        user.setCooldown("double_jump", plugin.getPermissionManager().getDoubleJumpDelay());

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));

        arena.getGame().getScoreboardManager().updateScoreboard(player);

        if (options.isEnabled(BooleanOption.JUMP_BAR)) {
            Utils.applyActionBarCooldown(user, plugin.getPermissionManager().getDoubleJumpDelay());
        }

        return true;
    }

    @EventHandler
    public void onLeaveItem(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        SpecialItem leaveItem = this.getLeaveItem(event);
        if (leaveItem == null) {
            return;
        }

        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);

        if (arena == null) {
            return;
        }

        event.setCancelled(true);

        User user = userManager.getUser(player);
        if (options.isEnabled(BooleanOption.INSTANT_LEAVE)) {
            arena.getGame().leaveUser(user);
            return;
        }

        UUID uuid = player.getUniqueId();
        if (!leaveConfirmations.add(uuid)) {
            leaveConfirmations.remove(uuid);

            user.sendMessage("game-items.leave-item.teleport-cancelled");
            return;
        }

        user.sendMessage("game-items.leave-item.returning-lobby");

        new BukkitRunnable() {

            private int ticks;

            @Override
            public void run() {
                if (!player.isOnline() ||
                    !arena.getGame().isPlaying(user) ||
                    !leaveConfirmations.contains(uuid)
                ) {
                    leaveConfirmations.remove(uuid);

                    cancel();
                    return;
                }

                if ((ticks += 2) >= 60) {
                    cancel();

                    arenaManager.leaveAttempt(user, PlayerLeaveGameEvent.LeaveReason.LEAVE_ITEM);
                    leaveConfirmations.remove(uuid);
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    private SpecialItem getLeaveItem(PlayerInteractEvent event) {
        SpecialItem item = itemManager.getItem("leave-item");
        return item != null && item.getOriginalItemStack().isSimilar(event.getItem()) ? item : null;
    }
}

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
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.menu.spectator.SpectatorSettingsMenu;
import dev.despical.tntrun.menu.spectator.SpectatorTeleportMenu;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.sound.GameSound;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

		if (!event.isFlying() || player.getGameMode() != GameMode.ADVENTURE) {
			return;
		}

		User user = plugin.getUserManager().getUser(player);
		Arena arena = user.getArena();

		if (!canUseDoubleJump(user, player, arena)) {
			return;
		}

        event.setCancelled(performDoubleJump(user, player, arena));
	}

	@EventHandler
	public void onDoubleJump(PlayerInteractEvent event) {
		Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

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

		if (isDoubleJumpItem(doubleJumpItem, event) && isActionAllowed(doubleJumpItem, event.getAction())) {
            Arena arena = user.getArena();
            if (!canUseDoubleJump(user, player, arena)) {
                return;
            }

			event.setCancelled(performDoubleJump(user, player, arena));
		}
	}

    private boolean canUseDoubleJump(User user, Player player, Arena arena) {
        return player.getGameMode() == GameMode.ADVENTURE &&
            arena != null &&
            arena.isArenaState(GameState.IN_GAME) &&
            !user.isSpectator() &&
            !arena.isDeathPlayer(user);
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

        Utils.restoreDoubleJumpFlightWhenReady(user, plugin.getPermissionManager().getDoubleJumpDelay());

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setVelocity(player.getLocation().getDirection().multiply(1.5D).setY(0.7D));
        plugin.getSoundManager().play(player, GameSound.DOUBLE_JUMP);

        arena.getGame().getScoreboardManager().updateScoreboard(player);

        if (options.isEnabled(BooleanOption.JUMP_BAR)) {
            Utils.applyActionBarCooldown(user, plugin.getPermissionManager().getDoubleJumpDelay());
        }

        return true;
    }

    private boolean isDoubleJumpItem(SpecialItem doubleJumpItem, PlayerInteractEvent event) {
        return doubleJumpItem.getOriginalItemStack().isSimilar(event.getItem());
    }

    private boolean isActionAllowed(SpecialItem item, Action action) {
        if (action == Action.PHYSICAL) {
            return false;
        }

        List<String> configuredActions = item.getCustomKey("actions");
        if (configuredActions == null || configuredActions.isEmpty()) {
            return action.isRightClick();
        }

        for (String configuredAction : configuredActions) {
            if (matchesAction(configuredAction, action)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesAction(String configuredAction, Action action) {
        String normalized = configuredAction
            .trim()
            .replace('-', '_')
            .toUpperCase(Locale.ENGLISH);

        return switch (normalized) {
            case "ANY", "BOTH", "CLICK" -> action.isLeftClick() || action.isRightClick();
            case "LEFT_CLICK" -> action.isLeftClick();
            case "RIGHT_CLICK" -> action.isRightClick();
            default -> action.name().equals(normalized);
        };
    }

    @EventHandler
    public void onSpectatorMenuItem(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;
        if (event.getItem() == null) return;

        SpecialItem matchingItem = getSpectatorMenuItem(event);
        if (matchingItem == null) {
            return;
        }

        User user = userManager.getUser(event.getPlayer());
        Arena arena = user.getArena();
        if (arena == null || !arena.isArenaState(GameState.IN_GAME) || !user.isSpectator()) {
            return;
        }

        event.setCancelled(true);
        if ("spectator-teleporter".equals(matchingItem.getKey())) {
            new SpectatorTeleportMenu(user).open();
            return;
        }

        new SpectatorSettingsMenu(user).open();
    }

    private SpecialItem getSpectatorMenuItem(PlayerInteractEvent event) {
        SpecialItem settingsItem = itemManager.getItemFromCategory("spectator-settings-menu-items", "spectator-settings");
        if (settingsItem != null && settingsItem.getOriginalItemStack().isSimilar(event.getItem())) {
            return settingsItem;
        }

        SpecialItem teleporterItem = itemManager.getItemFromCategory("spectator-teleporter-menu-items", "spectator-teleporter");
        if (teleporterItem != null && teleporterItem.getOriginalItemStack().isSimilar(event.getItem())) {
            return teleporterItem;
        }

        return null;
    }

    @EventHandler
    public void onPlayAgainItem(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        SpecialItem playAgainItem = getPlayAgainItem(event);
        if (playAgainItem == null) {
            return;
        }

        Player player = event.getPlayer();
        Arena currentArena = arenaRegistry.getArena(player);
        if (currentArena == null) {
            return;
        }

        event.setCancelled(true);

        User user = userManager.getUser(player);
        Optional<Arena> targetArena = findPlayAgainArena(currentArena);
        if (targetArena.isEmpty()) {
            user.sendMessage("play-again.no-arena-found");
            playConfiguredSound(player, playAgainItem, "no-arena-sound");
            return;
        }

        Arena target = targetArena.get();
        user.sendMessage("play-again.transferring", Var.of("%arena%", target.getId()));

        arenaManager.leaveAttempt(user, PlayerLeaveGameEvent.LeaveReason.LEAVE_ITEM);
        arenaManager.joinAttempt(user, target);
    }

    private Optional<Arena> findPlayAgainArena(Arena currentArena) {
        return arenaRegistry.getArenas().stream()
            .filter(arena -> !arena.equals(currentArena))
            .filter(Arena::isReady)
            .filter(Arena::isGameNonnull)
            .filter(arena -> arena.isArenaState(GameState.WAITING, GameState.STARTING))
            .filter(arena -> arena.getGame().getUsers().size() < arena.getMaximumPlayers())
            .findFirst();
    }

    private SpecialItem getPlayAgainItem(PlayerInteractEvent event) {
        SpecialItem item = itemManager.getItem("play-again");
        return item != null && item.getOriginalItemStack().isSimilar(event.getItem()) ? item : null;
    }

    private void playConfiguredSound(Player player, SpecialItem item, String key) {
        String rawSound = item.getCustomKey(key);
        if (rawSound == null || rawSound.isBlank()) {
            return;
        }

        String[] parts = rawSound.split(",");
        try {
            Sound sound = Sound.valueOf(parts[0].trim().toUpperCase(Locale.ENGLISH));
            float volume = parts.length > 1 ? Float.parseFloat(parts[1].trim()) : 1f;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2].trim()) : 1f;

            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
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

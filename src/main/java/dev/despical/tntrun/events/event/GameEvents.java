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

package dev.despical.tntrun.events.event;

import me.clip.placeholderapi.PlaceholderAPI;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.events.player.PlayerEliminatedEvent;
import dev.despical.tntrun.arena.ArenaState;
import dev.despical.tntrun.events.EventListener;
import dev.despical.tntrun.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Optional;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class GameEvents extends EventListener {

    public GameEvents(Main plugin) {
        super(plugin);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        var user = this.userManager.getUser(player);

        if (user.isInArena()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        if (this.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;

        var user = this.userManager.getUser(victim);
        var arena = user.getArena();

        if (arena == null) {
            return;
        }

        switch (e.getCause()) {
            case DROWNING, FALL -> e.setCancelled(true);
            case VOID -> {
                e.setCancelled(true);

                victim.teleport(arena.getLobbyLocation());

                if (!arena.isArenaState(ArenaState.IN_GAME)) {
                    return;
                }

                if (!user.isSpectator()) {
                    user.setSpectator(true);
                    user.playDeathEffect();
                    user.addGameItems("leave-item", "settings-item", "teleporter-item");

                    arena.addDeathPlayer(user);

                    plugin.getServer().getPluginManager().callEvent(new PlayerEliminatedEvent(arena, user));

                    if (arena.getPlayersLeft().size() == 1) {
                        arena.getWinners().add(arena.getWinner());
                        arena.broadcastFormattedMessage("messages.in-game.last-one-fell-into-void", user);

                        plugin.getArenaManager().stopGame(false, arena);
                        return;
                    }

                    arena.broadcastFormattedMessage("messages.in-game.fell-into-void", user);
                }
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player && this.isInArena(player)) {
            if (e.getView().getType() == InventoryType.CRAFTING || e.getView().getType() == InventoryType.PLAYER) {
                e.setResult(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        if (this.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (this.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Optional.ofNullable(this.userManager.getUser(player).getArena()).ifPresent(arena -> {
            if (arena.isArenaState(ArenaState.IN_GAME)) {
                return;
            }

            event.setCancelled(true);
            player.setFireTicks(0);
        });
    }

    @EventHandler
    public void onGeneralDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player)) return;

        User user = userManager.getUser(victim);

        Optional.ofNullable(user.getArena()).ifPresent(arena -> {
            if (!arena.isArenaState(ArenaState.IN_GAME)) {
                return;
            }

            if (BooleanOption.PVP_DISABLED.value()) {
                event.setCancelled(true);
            } else {
                if (!user.isSpectator()) {
                    event.setDamage(0);
                }
            }

            victim.setFireTicks(0);
        });
    }

    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        var user = this.userManager.getUser(event.getPlayer());
        var arena = user.getArena();

        if (arena == null) {
            if (!BooleanOption.DISABLE_SEPARATE_CHAT.value()) {
                plugin.getArenaRegistry().getArenas().forEach(loopArena -> loopArena.getPlayers().forEach(u -> event.getRecipients().remove(u.getPlayer())));
            }

            return;
        }

        if (BooleanOption.CHAT_FORMAT_ENABLED.value()) {
            var message = formatChatPlaceholders(chatManager.message("messages.in-game.game-chat-format"), user, event.getMessage());

            if (!BooleanOption.DISABLE_SEPARATE_CHAT.value()) {
                event.setCancelled(true);

                var dead = arena.isDeathPlayer(user) || arena.isSpectator(user);

                for (var u : arena.getPlayers()) {
                    if (dead && arena.getPlayersLeft().contains(u)) continue;

                    if (dead) {
                        String prefix = formatChatPlaceholders(chatManager.message("messages.in-game.game-death-format"), user, event.getMessage());
                        u.sendRawMessage(prefix + message);
                    } else {
                        u.sendRawMessage(message);
                    }
                }

                plugin.getServer().getConsoleSender().sendMessage(message);
            } else {
                event.setMessage(message);
            }
        }
    }

    private String formatChatPlaceholders(String message, User user, String saidMessage) {
        var formatted = message;

        formatted = formatted.replace("%player%", user.getName());
        formatted = formatted.replace("%message%", ChatColor.stripColor(saidMessage));

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            formatted = PlaceholderAPI.setPlaceholders(user.getPlayer(), formatted);
        }

        return chatManager.rawMessage(formatted);
    }
}

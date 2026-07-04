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

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ItemUtils;
import dev.despical.tntrun.utils.Schedulers;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
public class GeneralEvents extends ListenerAdapter {

    private final Map<UUID, Arena> quitPlayers = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        userManager.createNewUser(player);

        arenaRegistry.getArenas()
            .stream()
            .map(Arena::getGame)
            .filter(Objects::nonNull)
            .forEach(game -> game.getVisibilityManager().hidePlayerFromGame(player));

        Arena arena = quitPlayers.remove(player.getUniqueId());
        if (arena == null) {
            return;
        }

        Schedulers.runInTheNextTick(() -> {
            player.teleport(arena.getOption(ArenaKeys.END_LOCATION));

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);

            InventorySerializer.loadInventory(plugin, player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player);
        UUID uuid = user.getUUID();

        Arena arena = user.getArena();
        if (arena != null) {
            quitPlayers.put(uuid, arena);
            arenaManager.quitPlayer(user, arena);
        }

        userManager.removeUser(user);

        plugin.getStatsCacheManager().invalidate(uuid);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena arena = arenaRegistry.getArena(player);

        boolean separateChat = BooleanOption.CHAT_SEPARATE.value();
        Set<Player> recipients = event.getRecipients();

        if (arena == null) {
            if (!separateChat) return;

            arenaRegistry.getArenas().stream()
                .filter(Arena::isGameNonnull)
                .map(Arena::getGame)
                .flatMap(game -> game.getPlayers().stream())
                .forEach(recipients::remove);
            return;
        }

        event.setCancelled(true);

        if (BooleanOption.CHAT_DISABLE_IN_GAME.value()) {
            return;
        }

        User user = userManager.getUser(player);
        boolean deadChat = user.isSpectator() || arena.isDeathPlayer(user);
        Component formattedMessage = createChatMessage(player, event.getMessage(), deadChat);

        if (deadChat) {
            sendDeadChatMessage(arena.getGame(), formattedMessage);
            return;
        }

        sendAliveChatMessage(arena.getGame(), formattedMessage, separateChat);
    }

    private void sendAliveChatMessage(Game game, Component message, boolean separateChat) {
        if (!separateChat) {
            Bukkit.broadcast(message);
            return;
        }

        game.getPlayers().forEach(player -> player.sendMessage(message));
    }

    private void sendDeadChatMessage(Game game, Component message) {
        boolean visibleToAlive = BooleanOption.CHAT_DEAD_CHAT_VISIBLE_TO_ALIVE.value();

        game.getUsers().stream()
            .filter(user -> visibleToAlive || user.isSpectator() || game.getArena().isDeathPlayer(user))
            .map(User::getPlayer)
            .filter(Objects::nonNull)
            .forEach(player -> player.sendMessage(message));
    }

    private Component createChatMessage(Player player, String message, boolean deadChat) {
        if (!BooleanOption.CHAT_ENABLE_FORMATTING.value()) {
            return Component.text("<%s> %s".formatted(player.getName(), message));
        }

        return chatManager.getMessageComponent(
            deadChat ? "death-chat-format" : "chat-format",
            Var.of("%sender%", player.getName()),
            Var.of("%message%", message)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        cancelIfPlayerIsInGame(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFallDamage(EntityDamageEvent event) {
        var cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FALL
            && cause != EntityDamageEvent.DamageCause.VOID) return;

        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) return;

        event.setCancelled(true);

        Game game = arena.getGame();
        if (cause == EntityDamageEvent.DamageCause.VOID && game.isState(GameState.WAITING, GameState.STARTING)) {
            player.teleport(arena.getOption(ArenaKeys.LOBBY_LOCATION));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.getItem().remove();
        event.setCancelled(true);
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (arenaRegistry.isInArena(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    private <T extends EntityEvent & Cancellable> void cancelIfPlayerIsInGame(T event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Arena arena = arenaRegistry.getArena(player);
        if (arena == null) {
            return;
        }

        event.setCancelled(true);
    }
}

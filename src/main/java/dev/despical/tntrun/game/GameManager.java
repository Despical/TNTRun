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

package dev.despical.tntrun.game;

import dev.despical.commons.serializer.InventorySerializer;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.blocks.BlockRemovalConfig;
import dev.despical.tntrun.blocks.BlockSnapshotStore;
import dev.despical.tntrun.bossbar.BossBarConfig;
import dev.despical.tntrun.game.messages.MessageTicker;
import dev.despical.tntrun.game.visibility.VisibilityManager;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.ItemUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@Getter
public class GameManager {

    @Getter(AccessLevel.NONE)
    private final Main plugin;
    private final MessageTicker messageTicker;
    private final BossBarConfig bossBarConfig;
    private final BlockRemovalConfig blockRemovalConfig;
    private final BlockSnapshotStore blockSnapshotStore;

    public GameManager(Main plugin) {
        this.plugin = plugin;
        this.messageTicker = new MessageTicker();
        this.bossBarConfig = new BossBarConfig(plugin);
        this.blockRemovalConfig = new BlockRemovalConfig(plugin);
        this.blockSnapshotStore = new BlockSnapshotStore(plugin, blockRemovalConfig);
        this.blockSnapshotStore.restoreStartupSnapshots();
    }

    public void startGame(Game game) {
        game.setGameState(GameState.IN_GAME);
    }

    public void stopGame(Game game, StopReason reason) {
        if (game == null) return;

        game.getScoreboardManager().removeAllScoreboards();
        game.getBossBarManager().removeAll();
        game.broadcastMessage(reason.getMessagePath());

        VisibilityManager visibilityManager = game.getVisibilityManager();
        visibilityManager.showPlayersToEachOther();

        Arena arena = game.getArena();
        Location endLocation = arena.getOption(ArenaKeys.END_LOCATION);


        List<User> users = new ArrayList<>(game.getUsers());
        users.forEach(user -> {
            Player player = user.getPlayer();
            if (player == null) {
                return;
            }

            game.getScoreboardManager().removeScoreboard(player);
            game.getBossBarManager().removePlayer(player);

            player.clearTitle();
            player.sendActionBar(net.kyori.adventure.text.Component.empty());
            player.setItemOnCursor(null);
            player.setInvulnerable(false);
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setFireTicks(0);
            player.setExp(0F);
            player.setLevel(0);

            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            inventory.setArmorContents(ItemUtils.EMPTY_ARMORS);
            player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            InventorySerializer.loadInventory(plugin, player);
            player.teleport(endLocation);
            user.setSpectator(false);

            visibilityManager.showPlayerOutsideTheGame(player);
        });
        game.getUsers().clear();

        plugin.getEventManager().gameStop(game, reason);

        game.setGameState(GameState.RESTARTING);
    }

    public List<Game> getGames() {
        return plugin.getArenaRegistry().getArenas()
            .stream()
            .filter(Arena::isGameNonnull)
            .map(Arena::getGame)
            .toList();
    }

    public void reload() {
        bossBarConfig.load();
        blockRemovalConfig.reload();
        blockSnapshotStore.reload();

        getGames().forEach(game -> {
            game.getScoreboardManager().loadContents();
            game.getBossBarManager().update();
        });
    }

    public void shutdown() {
        blockSnapshotStore.shutdown();
    }
}

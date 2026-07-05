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

package dev.despical.tntrun.blocks;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
public class BlockRemovalManager {

    private static final Main plugin = Main.getInstance();

    private final Game game;
    private final BlockRemovalConfig config;
    private final BlockSnapshotStore store;
    private final Map<String, BlockSnapshot> snapshots;
    private final Set<String> queuedBlocks;
    private final Set<BukkitTask> delayedTasks;

    private BukkitTask scanTask;

    public BlockRemovalManager(Game game) {
        this.game = game;
        this.config = plugin.getGameManager().getBlockRemovalConfig();
        this.store = plugin.getGameManager().getBlockSnapshotStore();
        this.snapshots = new HashMap<>();
        this.queuedBlocks = new HashSet<>();
        this.delayedTasks = new HashSet<>();
    }

    public void start() {
        stopScanning();

        scanTask = Bukkit.getScheduler().runTaskTimer(plugin, this::scanPlayers, 0L, config.getScanIntervalTicks());
    }

    public void reset() {
        stopScanning();
        delayedTasks.forEach(BukkitTask::cancel);
        delayedTasks.clear();
        queuedBlocks.clear();

        snapshots.values().forEach(snapshot -> {
            snapshot.restore(false);
            store.remove(snapshot);
        });
        snapshots.clear();
    }

    public void stopScanning() {
        if (scanTask != null) {
            scanTask.cancel();
            scanTask = null;
        }
    }

    private void scanPlayers() {
        if (!game.isState(GameState.IN_GAME)) {
            stopScanning();
            return;
        }

        if (game.getTimer() <= config.getStartAfterSeconds()) {
            return;
        }

        for (User user : game.getPlayersLeft()) {
            Player player = user.getPlayer();

            if (player != null) {
                scanPlayer(player);
            }
        }
    }

    private void scanPlayer(Player player) {
        Location location = player.getLocation();
        int scanDepth = config.scanDepth(player.isOnGround());
        int y = location.getBlockY();

        for (int i = 0; i <= scanDepth; i++) {
            scheduleRemoval(getBlockUnderPlayer(y--, location));
        }
    }

    private void scheduleRemoval(Block block) {
        if (block == null || !config.isRemovable(block.getType())) {
            return;
        }

        String key = BlockSnapshot.key(block);
        if (queuedBlocks.contains(key) || snapshots.containsKey(key)) {
            return;
        }

        BlockSnapshot snapshot = BlockSnapshot.from(block);
        snapshots.put(key, snapshot);
        queuedBlocks.add(key);
        store.record(snapshot);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> removeBlock(snapshot), config.getRemoveDelayTicks());
        delayedTasks.add(task);
    }

    private void removeBlock(BlockSnapshot snapshot) {
        queuedBlocks.remove(snapshot.key());

        Block block = snapshot.getBlock();
        if (block == null || !snapshot.matches(block) || !config.isRemovable(block.getType())) {
            snapshots.remove(snapshot.key());
            store.remove(snapshot);
            return;
        }

        block.setType(Material.AIR, false);
    }

    private Block getBlockUnderPlayer(int y, Location location) {
        Position loc = new Position(location.getX(), y, location.getZ());
        Block b1 = loc.getBlock(location.getWorld(), 0.3, -0.3);

        if (b1.getType() != Material.AIR) {
            return b1;
        }

        Block b2 = loc.getBlock(location.getWorld(), -0.3, 0.3);

        if (b2.getType() != Material.AIR) {
            return b2;
        }

        Block b3 = loc.getBlock(location.getWorld(), 0.3, 0.3);

        if (b3.getType() != Material.AIR) {
            return b3;
        }

        Block b4 = loc.getBlock(location.getWorld(), -0.3, -0.3);

        if (b4.getType() != Material.AIR) {
            return b4;
        }

        return null;
    }

    private record Position(double x, int y, double z) {

        public Block getBlock(World world, double addx, double addz) {
            return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
        }
    }
}

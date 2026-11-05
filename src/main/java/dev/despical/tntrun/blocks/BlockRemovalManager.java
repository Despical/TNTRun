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

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
public class BlockRemovalManager {

    private static final TNTRun plugin = TNTRun.getInstance();

    private final Game game;
    private final BlockRemovalConfig config;
    private final BlockSnapshotStore store;
    private final Map<Long, BlockSnapshot> snapshots;
    private final ArrayDeque<PendingRemoval> pending;

    private BukkitTask scanTask;
    private BukkitTask drainTask;
    private boolean removalStarted;

    public BlockRemovalManager(Game game) {
        this.game = game;
        this.config = plugin.getGameManager().getBlockRemovalConfig();
        this.store = plugin.getGameManager().getBlockSnapshotStore();
        this.snapshots = new HashMap<>();
        this.pending = new ArrayDeque<>();
    }

    public void start() {
        stopScanning();
        removalStarted = false;

        scanTask = Schedulers.runTaskTimer(this::scanPlayers, 0L, config.getScanIntervalTicks());
    }

    public void reset() {
        stopScanning();
        stopDraining();

        pending.clear();
        removalStarted = false;

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

    public boolean hasRemovalStarted() {
        return removalStarted;
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
        World world = location.getWorld();

        int scanDepth = config.scanDepth(player.isOnGround());
        int y = location.getBlockY();

        int xHigh = NumberConversions.floor(location.getX() + 0.3);
        int xLow = NumberConversions.floor(location.getX() - 0.3);
        int zHigh = NumberConversions.floor(location.getZ() + 0.3);
        int zLow = NumberConversions.floor(location.getZ() - 0.3);

        for (int i = 0; i <= scanDepth; i++) {
            scheduleBlockUnder(world, xHigh, xLow, zHigh, zLow, y--);
        }
    }

    private void scheduleBlockUnder(World world, int xHigh, int xLow, int zHigh, int zLow, int y) {
        if (solid(world, xHigh, y, zLow)) {
            scheduleRemoval(world.getBlockAt(xHigh, y, zLow));
            return;
        }

        if (solid(world, xLow, y, zHigh)) {
            scheduleRemoval(world.getBlockAt(xLow, y, zHigh));
            return;
        }

        if (solid(world, xHigh, y, zHigh)) {
            scheduleRemoval(world.getBlockAt(xHigh, y, zHigh));
            return;
        }

        if (solid(world, xLow, y, zLow)) {
            scheduleRemoval(world.getBlockAt(xLow, y, zLow));
        }
    }

    private boolean solid(World world, int x, int y, int z) {
        return world.getType(x, y, z) != Material.AIR;
    }

    private void scheduleRemoval(Block block) {
        if (!config.isRemovable(block.getType())) {
            return;
        }

        long key = block.getBlockKey();
        if (snapshots.containsKey(key)) {
            return;
        }

        BlockSnapshot snapshot = BlockSnapshot.from(block);
        snapshots.put(key, snapshot);
        store.record(snapshot);

        pending.add(new PendingRemoval(snapshot, key, Bukkit.getCurrentTick() + config.getRemoveDelayTicks()));
        startDraining();
    }

    private void startDraining() {
        if (drainTask == null) {
            drainTask = Schedulers.runTaskTimer(this::drain, 1L, 1L);
        }
    }

    private void stopDraining() {
        if (drainTask != null) {
            drainTask.cancel();
            drainTask = null;
        }
    }

    private void drain() {
        int now = Bukkit.getCurrentTick();

        PendingRemoval next;
        while ((next = pending.peek()) != null && next.dueTick() <= now) {
            pending.poll();
            removeBlock(next);
        }

        if (pending.isEmpty()) {
            stopDraining();
        }
    }

    private void removeBlock(PendingRemoval removal) {
        BlockSnapshot snapshot = removal.snapshot();

        Block block = snapshot.getBlock();
        if (!snapshot.matches(block) || !config.isRemovable(block.getType())) {
            snapshots.remove(removal.key());
            store.remove(snapshot);
            return;
        }

        block.setType(Material.AIR, false);
        removalStarted = true;
    }

    private record PendingRemoval(BlockSnapshot snapshot, long key, int dueTick) {
    }
}

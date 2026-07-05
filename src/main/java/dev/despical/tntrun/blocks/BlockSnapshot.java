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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
public record BlockSnapshot(String key, String world, int x, int y, int z, String blockData) {

    public static BlockSnapshot from(Block block) {
        return new BlockSnapshot(
            key(block),
            block.getWorld().getName(),
            block.getX(),
            block.getY(),
            block.getZ(),
            block.getBlockData().getAsString()
        );
    }

    public static BlockSnapshot from(ConfigurationSection section) {
        if (section == null) {
            return null;
        }

        String world = section.getString("world");
        String blockData = section.getString("block-data");

        if (world == null || blockData == null) {
            return null;
        }

        int x = section.getInt("x");
        int y = section.getInt("y");
        int z = section.getInt("z");

        return new BlockSnapshot(key(world, x, y, z), world, x, y, z, blockData);
    }

    public static String key(Block block) {
        return key(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    private static String key(String world, int x, int y, int z) {
        return world + ";" + x + ";" + y + ";" + z;
    }

    public Block getBlock() {
        World bukkitWorld = Bukkit.getWorld(world);
        return bukkitWorld == null ? null : bukkitWorld.getBlockAt(x, y, z);
    }

    public boolean matches(Block block) {
        return block != null && block.getBlockData().getAsString().equals(blockData);
    }

    public boolean restore(boolean onlyIfAir) {
        Block block = getBlock();

        if (block == null || (onlyIfAir && !block.getType().isAir())) {
            return false;
        }

        try {
            BlockData data = Bukkit.createBlockData(blockData);
            block.setBlockData(data, false);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public void write(ConfigurationSection section) {
        section.set("world", world);
        section.set("x", x);
        section.set("y", y);
        section.set("z", z);
        section.set("block-data", blockData);
    }
}

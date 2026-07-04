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

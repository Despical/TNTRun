package dev.despical.tntrun.blocks;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.Main;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
@Getter
public class BlockRemovalConfig {

    private final Main plugin;

    private int startAfterSeconds;
    private int removeDelayTicks;
    private int scanIntervalTicks;
    private int scanDepthOnGround;
    private int scanDepthInAir;
    private boolean persistSnapshots;
    private boolean restoreSnapshotsOnStartup;
    private boolean restoreOnlyAir;
    private int flushIntervalTicks;
    private Set<Material> removableBlocks;

    public BlockRemovalConfig(Main plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        FileConfiguration config = ConfigUtils.getConfig(plugin, "block-removal");

        this.startAfterSeconds = config.getInt("timing.start-after-seconds", 5);
        this.removeDelayTicks = config.getInt("timing.remove-delay-ticks", 12);
        this.scanIntervalTicks = Math.max(1, config.getInt("timing.scan-interval-ticks", 1));
        this.scanDepthOnGround = Math.max(0, config.getInt("scanning.depth-on-ground", 2));
        this.scanDepthInAir = Math.max(0, config.getInt("scanning.depth-in-air", 6));
        this.persistSnapshots = config.getBoolean("safety.persist-snapshots", true);
        this.restoreSnapshotsOnStartup = config.getBoolean("safety.restore-snapshots-on-startup", true);
        this.restoreOnlyAir = config.getBoolean("safety.restore-only-air", true);
        int maxSafeFlushInterval = Math.max(1, removeDelayTicks - 1);
        this.flushIntervalTicks = Math.min(maxSafeFlushInterval, Math.max(1, config.getInt("safety.flush-interval-ticks", 5)));
        this.removableBlocks = Collections.unmodifiableSet(loadRemovableBlocks(config));
    }

    public boolean isRemovable(Material material) {
        return removableBlocks.contains(material);
    }

    public int scanDepth(boolean onGround) {
        return onGround ? scanDepthOnGround : scanDepthInAir;
    }

    private Set<Material> loadRemovableBlocks(FileConfiguration config) {
        Set<Material> materials = EnumSet.noneOf(Material.class);

        for (String blockName : config.getStringList("removable-blocks")) {
            Material material = Material.matchMaterial(blockName.trim().toUpperCase(Locale.ENGLISH));

            if (material == null || !material.isBlock()) {
                plugin.getLogger().warning("Invalid removable block in block-removal.yml: " + blockName);
                continue;
            }

            materials.add(material);
        }

        return materials;
    }
}

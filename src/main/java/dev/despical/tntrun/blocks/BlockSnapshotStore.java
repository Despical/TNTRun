package dev.despical.tntrun.blocks;

import dev.despical.tntrun.Main;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
public class BlockSnapshotStore {

    private final Main plugin;
    private final BlockRemovalConfig config;
    private final File file;
    private final Map<String, BlockSnapshot> snapshots;

    private BukkitTask flushTask;
    private boolean dirty;

    public BlockSnapshotStore(Main plugin, BlockRemovalConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.file = new File(plugin.getDataFolder(), "data/removed-blocks.yml");
        this.snapshots = new LinkedHashMap<>();

        loadFromDisk();
        rescheduleFlushTask();
    }

    public void reload() {
        if (!config.isPersistSnapshots() && !snapshots.isEmpty()) {
            snapshots.clear();
            dirty = true;

            flushNow(true);
        }

        rescheduleFlushTask();
    }

    public void restoreStartupSnapshots() {
        if (!config.isRestoreSnapshotsOnStartup() || snapshots.isEmpty()) {
            return;
        }

        int restored = 0;

        for (BlockSnapshot snapshot : snapshots.values()) {
            if (snapshot.restore(config.isRestoreOnlyAir())) {
                restored++;
            }
        }

        snapshots.clear();
        dirty = true;

        flushNow(true);

        if (restored > 0) {
            plugin.getLogger().info("Restored " + restored + " pending TNT Run blocks from crash recovery.");
        }
    }

    public void record(BlockSnapshot snapshot) {
        if (!config.isPersistSnapshots()) {
            return;
        }

        snapshots.put(snapshot.key(), snapshot);
        dirty = true;
    }

    public void remove(BlockSnapshot snapshot) {
        if (!config.isPersistSnapshots()) {
            return;
        }

        if (snapshots.remove(snapshot.key()) != null) {
            dirty = true;
        }
    }

    public void flushNow() {
        flushNow(false);
    }

    private void flushNow(boolean force) {
        if (!dirty || (!force && !config.isPersistSnapshots())) {
            return;
        }

        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("blocks");

        for (BlockSnapshot snapshot : snapshots.values()) {
            snapshot.write(section.createSection(snapshot.key()));
        }

        try {
            yaml.save(file);

            dirty = false;
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not save block removal recovery file.", ex);
        }
    }

    public void shutdown() {
        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }

        flushNow();
    }

    private void loadFromDisk() {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("blocks");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            BlockSnapshot snapshot = BlockSnapshot.from(section.getConfigurationSection(key));

            if (snapshot != null) {
                snapshots.put(snapshot.key(), snapshot);
            }
        }
    }

    private void rescheduleFlushTask() {
        if (flushTask != null) {
            flushTask.cancel();
        }

        flushTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> flushNow(), config.getFlushIntervalTicks(), config.getFlushIntervalTicks());
    }
}

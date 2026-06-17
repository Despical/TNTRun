package dev.despical.tntrun.option;

import dev.despical.tntrun.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class ConfigOptions {

    private final Main plugin;
    private final Map<ConfigOption<?>, Object> options;

    public ConfigOptions(Main plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();
        this.loadOptions();
    }

    public <T> T get(ConfigOption<T> option) {
        return option.getType().cast(options.computeIfAbsent(option, opt -> option.getDefaultValue()));
    }

    public boolean isEnabled(BooleanOption option) {
        return this.<Boolean>get(option);
    }

    public void reloadOptions() {
        plugin.reloadConfig();

        loadOptions();
    }

    private void loadOptions() {
        FileConfiguration config = plugin.getConfig();

        Stream.of(BooleanOption.values())
            .forEach(option -> options.put(option, config.get(option.getPath(), option.getDefaultValue())));
    }
}

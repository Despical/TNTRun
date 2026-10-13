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

package dev.despical.tntrun.option;

import dev.despical.tntrun.TNTRun;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class ConfigOptions {

    private static final EnumSet<IntOption> TIMER_OPTIONS = EnumSet.of(
        IntOption.LOBBY_WAITING_TIME,
        IntOption.PRE_GAME_WAITING_TIME,
        IntOption.GAME_STARTING_TIME,
        IntOption.FULL_GAME_STARTING_TIME,
        IntOption.ENDING_TIME
    );

    private final TNTRun plugin;
    private final Map<ConfigOption<?>, Object> options;

    public ConfigOptions(TNTRun plugin) {
        this.plugin = plugin;
        this.options = new HashMap<>();
        this.loadOptions();
    }

    public <T> T get(ConfigOption<T> option) {
        return option.getType().cast(options.computeIfAbsent(option, _ -> option.getDefaultValue()));
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

        Stream.of(IntOption.values(), BooleanOption.values())
            .flatMap(Arrays::stream)
            .forEach(option -> loadOption(option, config));
    }

    private void loadOption(ConfigOption<?> option, FileConfiguration config) {
        Object value = config.get(option.getPath(), option.getDefaultValue());

        if (option instanceof IntOption intOption && TIMER_OPTIONS.contains(intOption)) {
            int timer = config.getInt(intOption.getPath(), intOption.getDefaultValue());

            if (timer < 0) {
                plugin.getLogger().warning("Invalid negative value for '%s': %d. Using default: %d."
                    .formatted(intOption.getPath(), timer, intOption.getDefaultValue()));

                timer = intOption.getDefaultValue();
            }

            value = timer;
        }

        options.put(option, value);
    }
}

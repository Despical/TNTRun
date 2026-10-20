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

package dev.despical.tntrun.rewards;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Immutable command configuration shared by reward dispatches until reload.
 *
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
record RewardConfiguration(
    boolean enabled, Map<RewardType,
    List<String>> commands,
    Map<RewardType, Map<Integer, List<String>>> milestones,
    int survivalInterval,
    BigDecimal survivalAmount
) {

    static RewardConfiguration load(FileConfiguration config, Logger logger) {
        EnumMap<RewardType, List<String>> commands = new EnumMap<>(RewardType.class);
        EnumMap<RewardType, Map<Integer, List<String>>> milestones = new EnumMap<>(RewardType.class);

        for (RewardType type : RewardType.values()) {
            String path = type.getConfigurationPath();

            if (type.hasThresholds()) {
                milestones.put(type, loadMilestones(config.getConfigurationSection(path + ".thresholds"), logger));
            } else {
                commands.put(type, List.copyOf(config.getStringList(path + (type == RewardType.SURVIVAL_TIME ? ".commands" : ""))));
            }
        }

        int interval;
        BigDecimal amount;

        try {
            interval = Integer.parseInt(config.getString("survival-time.interval-seconds", "10"));
            amount = new BigDecimal(config.getString("survival-time.amount-per-interval", "1"));
            if (interval <= 0 || amount.signum() < 0) {
                throw new IllegalArgumentException("Expected a positive interval and a non-negative amount");
            }
        } catch (IllegalArgumentException exception) {
            logger.warning("Invalid survival-time settings in rewards.yml. Survival payments are disabled until corrected.");
            interval = 1;
            amount = BigDecimal.ZERO;
        }

        return new RewardConfiguration(config.getBoolean("enabled", true), Map.copyOf(commands), Map.copyOf(milestones), interval, amount);
    }

    private static Map<Integer, List<String>> loadMilestones(ConfigurationSection section, Logger logger) {
        if (section == null) {
            return Map.of();
        }

        Map<Integer, List<String>> thresholds = new TreeMap<>();

        for (String key : section.getKeys(false)) {
            try {
                int threshold = Integer.parseInt(key);
                if (threshold <= 0) {
                    throw new NumberFormatException();
                }

                thresholds.put(threshold, List.copyOf(section.getStringList(key)));
            } catch (NumberFormatException exception) {
                logger.warning("Ignoring invalid reward threshold '" + key + "' in " + section.getCurrentPath() + ". Use positive whole numbers.");
            }
        }

        return Map.copyOf(thresholds);
    }

    List<String> commands(RewardType type, int threshold) {
        return type.hasThresholds()
            ? milestones.getOrDefault(type, Map.of()).getOrDefault(threshold, List.of())
            : commands.getOrDefault(type, List.of());
    }

    String survivalReward(int seconds) {
        return survivalAmount.multiply(BigDecimal.valueOf(Math.max(0, seconds) / survivalInterval)).stripTrailingZeros().toPlainString();
    }
}

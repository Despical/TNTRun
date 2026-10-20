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

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Despical
 * <p>
 * Created at 20.10.2026
 */
class RewardConfigurationTest {

    private final YamlConfiguration config = new YamlConfiguration();
    private final Logger logger = Logger.getLogger("RewardConfigurationTest");

    @Test
    void bundledConfigurationHasNoActiveCommandsForAnyReward() {
        var stream = getClass().getResourceAsStream("/rewards.yml");
        assertNotNull(stream);

        var defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        RewardConfiguration configuration = RewardConfiguration.load(defaults, logger);
        assertTrue(configuration.enabled());

        for (RewardType type : RewardType.values()) {
            assertTrue(configuration.commands(type, 0).isEmpty(), type.name());

            configuration.milestones().getOrDefault(type, java.util.Map.of()).values().forEach(commands -> assertTrue(commands.isEmpty()));
        }

        assertEquals("30", configuration.survivalReward(65));
    }

    @Test
    void calculatesWholeIntervalsWithExactDecimalAmounts() {
        config.set("survival-time.interval-seconds", 10);
        config.set("survival-time.amount-per-interval", "0.15");

        RewardConfiguration configuration = load();

        assertEquals("0", configuration.survivalReward(9));
        assertEquals("0.15", configuration.survivalReward(10));
        assertEquals("0.9", configuration.survivalReward(65));
        assertEquals("0", configuration.survivalReward(-1));
    }

    @Test
    void invalidIntervalDisablesOnlySurvivalPayments() {
        config.set("survival-time.interval-seconds", 0);
        config.set("first-place", List.of("eco give %player% 100"));

        RewardConfiguration configuration = load();

        assertEquals("0", configuration.survivalReward(65));
        assertFalse(configuration.commands(RewardType.FIRST_PLACE, 0).isEmpty());
    }

    @Test
    void invalidAmountsCannotProduceNegativeOrNonFinitePayments() {
        for (String amount : List.of("-1", "NaN", "Infinity", "invalid")) {
            config.set("survival-time.amount-per-interval", amount);
            assertEquals("0", load().survivalReward(60));
        }
    }

    @Test
    void fractionalOrNonNumericIntervalsAreRejected() {
        for (Object interval : List.of("invalid", 1.5, -1)) {
            config.set("survival-time.interval-seconds", interval);
            assertEquals("0", load().survivalReward(60));
        }
    }

    @Test
    void uncommentingTheBundledExamplesProducesUsableLists() throws Exception {
        var stream = getClass().getResourceAsStream("/rewards.yml");
        assertNotNull(stream);

        String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        YamlConfiguration examples = new YamlConfiguration();
        examples.loadFromString(text.replace("# - '", "- '"));

        RewardConfiguration configuration = RewardConfiguration.load(examples, logger);

        for (RewardType type : RewardType.values()) {
            if (type.hasThresholds()) {
                assertFalse(configuration.milestones().get(type).isEmpty(), type.name());
                configuration.milestones().get(type).values().forEach(commands -> assertFalse(commands.isEmpty()));
            } else {
                assertFalse(configuration.commands(type, 0).isEmpty(), type.name());
            }
        }
    }

    @Test
    void invalidThresholdsAreIgnoredAndValidCommandsSurvive() {
        config.set("survival-milestone.thresholds.30", List.of("valid"));
        config.set("survival-milestone.thresholds.0", List.of("zero"));
        config.set("survival-milestone.thresholds.-2", List.of("negative"));
        config.set("survival-milestone.thresholds.invalid", List.of("invalid"));
        RewardConfiguration configuration = load();

        assertEquals(java.util.Set.of(30), configuration.milestones().get(RewardType.SURVIVAL_MILESTONE).keySet());
        assertEquals(List.of("valid"), configuration.commands(RewardType.SURVIVAL_MILESTONE, 30));
    }

    @Test
    void loadedCommandsDoNotChangeWhenTheSourceConfigurationChanges() {
        config.set("first-place", List.of("original"));
        config.set("win-streak.thresholds.3", List.of("streak"));
        RewardConfiguration configuration = load();
        config.set("first-place", List.of("changed"));
        config.set("win-streak.thresholds.3", List.of("changed"));

        assertEquals(List.of("original"), configuration.commands(RewardType.FIRST_PLACE, 0));
        assertEquals(List.of("streak"), configuration.commands(RewardType.WIN_STREAK, 3));
        assertThrows(UnsupportedOperationException.class, () -> configuration.commands(RewardType.FIRST_PLACE, 0).add("mutated"));
    }

    private RewardConfiguration load() {
        return RewardConfiguration.load(config, logger);
    }
}

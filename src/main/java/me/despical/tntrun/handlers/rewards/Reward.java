/*
 * TNT Run - A Minecraft parkour minigame
 * Copyright (C) 2024  Berke Akçen
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

package me.despical.tntrun.handlers.rewards;

import me.despical.tntrun.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class Reward {

    private final RewardType type;
    private final List<SubReward> rewards;

    public Reward(Main plugin, RewardType type, List<String> rawCodes) {
        this.type = type;
        this.rewards = new ArrayList<>();

        for (var rawCode : rawCodes) {
            this.rewards.add(new SubReward(plugin, rawCode));
        }
    }

    List<SubReward> getRewards() {
        return rewards;
    }

    public RewardType getType() {
        return type;
    }

    public enum RewardType {

        WIN("Win"),
        LOSE("Lose"),
        END_GAME("End-Game"),
        DOUBLE_JUMP("Double-Jump");

        final String path;

        RewardType(String path) {
            this.path = "Rewards." + path;
        }
    }

    static final class SubReward {

        private final int chance, executor;
        private String executableCode;

        public SubReward(Main plugin, String rawCode) {
            var processedCode = rawCode;

            if (rawCode.contains("p:")) {
                this.executor = 2;

                processedCode = processedCode.replace("p:", "");
            } else {
                this.executor = 1;
            }

            if (processedCode.contains("chance(")) {
                var loc = processedCode.indexOf(")");

                if (loc == -1) {
                    plugin.getLogger().warning("Second '')'' is not found in chance condition! Command: %s".formatted(rawCode));

                    this.chance = 101;
                    return;
                }

                var chanceStr = processedCode;
                chanceStr = chanceStr.substring(0, loc).replaceAll("[^0-9]+", "");

                processedCode = processedCode.replace("chance(%s):".formatted(chanceStr), "");

                this.chance = Integer.parseInt(chanceStr);
            } else {
                this.chance = 100;
            }

            this.executableCode = processedCode;
        }

        public String getExecutableCode() {
            return executableCode;
        }

        public int getExecutor() {
            return executor;
        }

        public int getChance() {
            return chance;
        }
    }
}
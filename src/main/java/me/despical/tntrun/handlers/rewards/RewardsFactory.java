/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2020 Despical
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

import me.despical.commonsbox.configuration.ConfigUtils;
import me.despical.commonsbox.engine.ScriptEngine;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaRegistry;
import me.despical.tntrun.utils.Debugger;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class RewardsFactory {

	private final Set<Reward> rewards = new HashSet<>();
	private final FileConfiguration config;
	private final boolean enabled;

	public RewardsFactory(Main plugin) {
		enabled = plugin.getConfig().getBoolean("Rewards-Enabled");
		config = ConfigUtils.getConfig(plugin, "rewards");

		registerRewards();
	}

	public void performReward(Arena arena, Reward.RewardType type) {
		if (!enabled) {
			return;
		}

		for (Player player : arena.getPlayers()) {
			performReward(player, type);
		}
	}

	public void performReward(Player player, Reward.RewardType type) {
		if (!enabled) {
			return;
		}

		Arena arena = ArenaRegistry.getArena(player);

		for (Reward reward : rewards) {
			if (reward.getType() == type) {
				if (reward.getChance() != -1 && ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) {
					continue;
				}

				String command = reward.getExecutableCode();
				command = StringUtils.replace(command, "%player%", player.getName());
				command = formatCommandPlaceholders(command, arena);

				switch (reward.getExecutor()) {
					case CONSOLE:
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
						break;
					case PLAYER:
						player.performCommand(command);
						break;
					case SCRIPT:
						ScriptEngine engine = new ScriptEngine();

						engine.setValue("player", player);
						engine.setValue("server", Bukkit.getServer());
						engine.setValue("arena", arena);
						engine.execute(command);
						break;
					default:
						break;
				}
			}
		}
	}

	private String formatCommandPlaceholders(String command, Arena arena) {
		String formatted = command;

		formatted = StringUtils.replace(formatted, "%arena-id%", arena.getId());
		formatted = StringUtils.replace(formatted, "%mapname%", arena.getMapName());
		formatted = StringUtils.replace(formatted, "%players%", String.valueOf(arena.getPlayers().size()));
		return formatted;
	}

	private void registerRewards() {
		if (!enabled) {
			return;
		}

		Debugger.debug("[RewardsFactory] Starting rewards registration");
		long start = System.currentTimeMillis();
		Map<Reward.RewardType, Integer> registeredRewards = new HashMap<>();

		for (Reward.RewardType rewardType : Reward.RewardType.values()) {
			for (String reward : config.getStringList("rewards." + rewardType.getPath())) {
				rewards.add(new Reward(rewardType, reward));
				registeredRewards.put(rewardType, registeredRewards.getOrDefault(rewardType, 0) + 1);
			}
		}

		for (Reward.RewardType rewardType : registeredRewards.keySet()) {
			Debugger.debug("[RewardsFactory] Registered {0} {1} rewards!", registeredRewards.get(rewardType), rewardType.name());
		}

		Debugger.debug("[RewardsFactory] Registered all rewards took {0} ms", System.currentTimeMillis() - start);
	}
}
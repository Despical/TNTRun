/*
 * TNT Run - Don't stop running to win!
 * Copyright (C) 2023 Despical
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

import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.engine.ScriptEngine;
import me.despical.tntrun.Main;
import me.despical.tntrun.user.User;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class RewardsFactory {

	private final Main plugin;
	private final Set<Reward> rewards;

	public RewardsFactory(final Main plugin) {
		this.plugin = plugin;
		this.rewards = new HashSet<>();

		registerRewards();
	}

	public void performReward(final User user, final Reward.RewardType type) {
		final var reward = rewards.stream().filter(rew -> rew.getType() == type).findFirst().orElse(null);

		if (reward == null) return;
		if (ThreadLocalRandom.current().nextInt(0, 100) > reward.getChance()) return;

		final var arena = user.getArena();
		final var player = user.getPlayer();
		final var command = formatCommandPlaceholders(reward, user);

		switch (reward.getExecutor()) {
			case 1 -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
			case 2 -> player.performCommand(command);
			case 3 -> {
				final var engine = new ScriptEngine();
				engine.setValue("player", player);
				engine.setValue("server", plugin.getServer());
				engine.setValue("arena", arena);
				engine.execute(command);
			}
			default -> {
			}
		}
	}

	private String formatCommandPlaceholders(final Reward reward, final User user) {
		var arena = user.getArena();
		var formatted = reward.getExecutableCode();

		formatted = formatted.replace("%arena%", arena.getId());
		formatted = formatted.replace("%map_name%", arena.getMapName());
		formatted = formatted.replace("%player%", user.getName());
		formatted = formatted.replace("%players%", Integer.toString(arena.getPlayers().size()));
		return formatted;
	}

	private void registerRewards() {
		var config = ConfigUtils.getConfig(plugin, "rewards");

		if (!config.getBoolean("Rewards-Enabled")) return;

		for (final var rewardType : Reward.RewardType.values()) {
			for (final var reward : config.getStringList(rewardType.path)) {
				rewards.add(new Reward(plugin, rewardType, reward));
			}
		}
	}
}
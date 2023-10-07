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

package me.despical.tntrun.user;

import me.despical.commons.miscellaneous.AttributeUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.StatsStorage;
import me.despical.tntrun.api.events.player.TRPlayerStatisticChangeEvent;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.handlers.rewards.Reward;
import me.despical.tntrun.utils.Utils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class User {

	private static final Main plugin = JavaPlugin.getPlugin(Main.class);
	private static long cooldownCounter;

	private final Player player;
	private final Map<String, Double> cooldowns;
	private final Map<StatsStorage.StatisticType, Integer> stats;

	private boolean spectator;
	private Scoreboard cachedScoreboard;

	public User(Player player) {
		this.player = player;
		this.cooldowns = new HashMap<>();
		this.stats = new EnumMap<>(StatsStorage.StatisticType.class);
	}

	public void sendMessage(final String path) {
		this.sendRawMessage(plugin.getChatManager().message(path));
	}

	public void sendMessage(final String path, final Object... args) {
		this.sendRawMessage(plugin.getChatManager().message(path), args);
	}

	public void sendRawMessage(final String message) {
		this.player.sendMessage(plugin.getChatManager().rawMessage(message));
	}

	public void sendRawMessage(final String message, final Object... args) {
		this.player.sendMessage(plugin.getChatManager().rawMessage(String.format(message, args)));
	}

	public void performReward(final Reward.RewardType rewardType) {
		plugin.getRewardsFactory().performReward(this, rewardType);
	}


	public void closeOpenedInventory() {
		this.player.closeInventory();
	}

	public boolean isInArena() {
		return plugin.getArenaRegistry().isInArena(this);
	}

	@Nullable
	public Arena getArena() {
		return plugin.getArenaRegistry().getArena(this);
	}

	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return player.getName();
	}

	public Location getLocation() {
		return player.getLocation();
	}

	public UUID getUniqueId() {
		return player.getUniqueId();
	}

	public boolean isSpectator() {
		return spectator;
	}

	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}

	public int getStat(StatsStorage.StatisticType stat) {
		final var statistic = stats.get(stat);

		if (statistic == null) {
			stats.put(stat, 0);
			return 0;
		}

		return statistic;
	}

	public void setStat(StatsStorage.StatisticType stat, int value) {
		stats.put(stat, value);

		if (plugin.isEnabled())
			plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new TRPlayerStatisticChangeEvent(getArena(), player, stat, value)));
	}

	public void addStat(StatsStorage.StatisticType stat, int value) {
		setStat(stat, getStat(stat) + value);
	}

	public boolean hasPermission(final String permission) {
		return this.player.hasPermission(permission);
	}

	public void setCooldown(String s, double seconds) {
		cooldowns.put(s, seconds + cooldownCounter);
	}

	public double getCooldown(String s) {
		final var cooldown = cooldowns.get(s);

		return (cooldown == null || cooldown <= cooldownCounter) ? 0 : cooldown - cooldownCounter;
	}

	public void resetTemporaryStats() {
		for (final var statistic : StatsStorage.StatisticType.values()) {
			if (statistic.isPersistent()) continue;

			setStat(statistic, 0);
		}

		setStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, plugin.getPermissionManager().getDoubleJumps(this.player));

		this.spectator = false;
	}

	public void heal() {
		if (plugin.getOption(ConfigPreferences.Option.HEAL_PLAYER)) AttributeUtils.healPlayer(player);
	}

	public void applyDoubleJumpDelay() {
		final int cooldown = plugin.getPermissionManager().getDoubleJumpDelay();

		addStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS, -1);
		setCooldown("double_jump", cooldown);
		performReward(Reward.RewardType.DOUBLE_JUMP);

		if (plugin.getOption(ConfigPreferences.Option.JUMP_BAR) && getStat(StatsStorage.StatisticType.LOCAL_DOUBLE_JUMPS) > 0)
			Utils.applyActionBarCooldown(this, cooldown);
	}

	public void removePotionEffectsExcept(final PotionEffectType... effectTypes) {
		final var setOfEffects = Set.of(effectTypes);

		for (final var activePotion : this.player.getActivePotionEffects()) {
			if (setOfEffects.contains(activePotion.getType())) continue;

			player.removePotionEffect(activePotion.getType());
		}
	}

	public void addGameItems(final String... ids) {
		this.addGameItems(true, ids);
	}

	@SuppressWarnings("all")
	public void sendActionBar(@NotNull String message) {
		try {
			this.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
		} catch (Exception | Error ignored) { }
	}

	public void addGameItems(boolean clearInventory, final String... ids) {
		if (clearInventory) this.player.getInventory().clear();

		for (final var id : ids) {
			this.addGameItem(id);
		}

		this.player.updateInventory();
	}

	public void addGameItem(final String id) {
		final var gameItem = plugin.getGameItemManager().getGameItem(id);

		if (gameItem == null) return;

		this.player.getInventory().setItem(gameItem.getSlot(), gameItem.getItemStack());
	}

	public void playDeathEffect() {
		player.setAllowFlight(true);
		player.setFlying(true);
		player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 4 * 20, 1, false, false, false));
	}

	public void cacheScoreboard() {
		this.cachedScoreboard = this.player.getScoreboard();
	}

	public void removeScoreboard() {
		if (this.cachedScoreboard == null) return;

		this.player.setScoreboard(this.cachedScoreboard);
		this.cachedScoreboard = null;
	}

	public static void cooldownHandlerTask() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
	}
}
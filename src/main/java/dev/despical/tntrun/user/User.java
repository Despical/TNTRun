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

package dev.despical.tntrun.user;

import com.cryptomorin.xseries.messages.ActionBar;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.events.player.StatisticChangeEvent;
import dev.despical.tntrun.api.statistic.StatisticType;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.handlers.rewards.Reward;
import dev.despical.tntrun.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class User {

    private static final Main plugin = JavaPlugin.getPlugin(Main.class);
    private static long cooldownCounter;

    private final UUID uuid;
    private final String playerName;
    private final Map<String, Double> cooldowns;
    private final Map<StatisticType, Integer> stats;

    private boolean spectator;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.cooldowns = new HashMap<>();
        this.stats = new EnumMap<>(StatisticType.class);
    }

    public static void cooldownHandlerTask() {
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> cooldownCounter++, 20, 20);
    }

    public void sendMessage(final String path) {
        this.sendRawMessage(plugin.getChatManager().message(path, this));
    }

    public void sendMessage(final String path, final Object... args) {
        this.sendRawMessage(plugin.getChatManager().message(path, this), args);
    }

    public void sendRawMessage(final String message) {
        this.getPlayer().sendMessage(plugin.getChatManager().rawMessage(message));
    }

    public void sendRawMessage(final String message, final Object... args) {
        this.getPlayer().sendMessage(plugin.getChatManager().rawMessage(MessageFormat.format(message, args)));
    }

    public void performReward(final Reward.RewardType rewardType) {
        plugin.getRewardsFactory().performReward(this, rewardType);
    }

    public void closeOpenedInventory() {
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> this.getPlayer().closeInventory(), 1L);
    }

    public boolean isInArena() {
        return plugin.getArenaRegistry().isInArena(this);
    }

    @Nullable
    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(this);
    }

    public Player getPlayer() {
        return plugin.getServer().getPlayer(uuid);
    }

    public String getName() {
        return playerName;
    }

    public Location getLocation() {
        return getPlayer().getLocation();
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public boolean isSpectator() {
        return spectator;
    }

    public void setSpectator(boolean spectator) {
        this.spectator = spectator;
    }

    public int getStat(StatisticType statisticType) {
        return stats.computeIfAbsent(statisticType, stat -> 0);
    }

    public void setStat(StatisticType stat, int value) {
        stats.put(stat, value);

        if (plugin.isEnabled()) {
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getServer().getPluginManager().callEvent(new StatisticChangeEvent(getArena(), this, stat, value)));
        }
    }

    public void addStat(StatisticType stat, int value) {
        setStat(stat, getStat(stat) + value);
    }

    public boolean hasPermission(final String permission) {
        return this.getPlayer().hasPermission(permission);
    }

    public void setCooldown(String s, double seconds) {
        cooldowns.put(s, seconds + cooldownCounter);
    }

    public double getCooldown(String s) {
        Double cooldown = cooldowns.get(s);

        return (cooldown == null || cooldown <= cooldownCounter) ? 0 : cooldown - cooldownCounter;
    }

    public void resetTemporaryStats() {
        for (StatisticType stat : StatisticType.PERSISTENT_STATS) {
            setStat(stat, 0);
        }

        this.setStat(StatisticType.LOCAL_DOUBLE_JUMPS, plugin.getPermissionManager().getDoubleJumps(this.getPlayer()));
        this.spectator = false;
    }

    public void applyDoubleJumpDelay() {
        final int cooldown = plugin.getPermissionManager().getDoubleJumpDelay();

        addStat(StatisticType.LOCAL_DOUBLE_JUMPS, -1);
        setCooldown("double_jump", cooldown);
        performReward(Reward.RewardType.DOUBLE_JUMP);

        if (BooleanOption.JUMP_BAR.value() && getStat(StatisticType.LOCAL_DOUBLE_JUMPS) > 0)
            Utils.applyActionBarCooldown(this, cooldown);
    }

    public void removePotionEffectsExcept(PotionEffectType... potions) {
        Set<PotionEffectType> setOfEffects = Set.of(potions);
        Player player = this.getPlayer();

        for (final var activePotion : player.getActivePotionEffects()) {
            if (setOfEffects.contains(activePotion.getType())) continue;

            player.removePotionEffect(activePotion.getType());
        }
    }

    public void addGameItems(final String... ids) {
        this.addGameItems(true, ids);
    }

    public void sendActionBar(@NotNull String message) {
        ActionBar.sendActionBar(this.getPlayer(), message);
    }

    public void addGameItems(boolean clearInventory, final String... ids) {
        var player = this.getPlayer();

        if (clearInventory) {
            player.getInventory().clear();
        }

        for (final var id : ids) {
            this.addGameItem(id);
        }

        player.updateInventory();
    }

    public void addGameItem(final String id) {
        plugin.getItemManager().findItem(id).ifPresent(gameItem -> this.getPlayer().getInventory().setItem(gameItem.<Integer>getCustomKey("slot"), gameItem.getItemStack()));
    }

    public void playDeathEffect() {
        final var player = this.getPlayer();

        player.setAllowFlight(true);
        player.setFlying(true);
//        player.addPotionEffect(XPotion.INVISIBILITY.buildPotionEffect(Integer.MAX_VALUE, 2));
//        player.addPotionEffect(XPotion.BLINDNESS.buildPotionEffect(4 * 20, 2));
    }
}

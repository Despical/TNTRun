/*
TNT Run - Fast-paced arena survival for Minecraft.
Copyright (C) 2026  Berke Akçen

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package dev.despical.tntrun.user;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.event.player.PlayerStatisticChangeEvent;
import dev.despical.tntrun.arena.Arena;
import dev.despical.tntrun.stats.StatisticType;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.utils.Var;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class User {

    private static final Main plugin = Main.getInstance();

    @Getter
    @Setter
    private boolean spectator;

    private final UUID uuid;

    @Getter
    private final String name;

    private final Map<String, Long> cooldowns;
    private final Map<StatisticType<?>, Object> stats;

    public User(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.cooldowns = new HashMap<>();
        this.stats = new HashMap<>();
    }

    public UUID getUUID() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public void ifPlayerPresent(Consumer<Player> playerConsumer) {
        Player player = getPlayer();
        if (player != null) {
            playerConsumer.accept(player);
        }
    }

    public void sendMessage(String path, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendMessage(player, path, variables));
    }

    public void sendRawMessage(String msg, Var... variables) {
        ifPlayerPresent(player -> {
            Component message = plugin.getChatManager().parseMessage(msg, variables);
            player.sendMessage(message);
        });
    }

    public void sendRawMessage(String msg, Object... values) {
        for (int i = 0; i < values.length; i++) {
            msg = msg.replace("{" + i + "}", String.valueOf(values[i]));
        }

        sendRawMessage(msg);
    }

    public void closeOpenedInventory() {
        ifPlayerPresent(Player::closeInventory);
    }

    public Location getLocation() {
        Player player = getPlayer();
        return player == null ? null : player.getLocation();
    }

    public void sendRawActionBarComponent(Component message) {
        ifPlayerPresent(player -> player.sendActionBar(message));
    }

    public void sendRawComponent(Component component, Var... vars) {
        ifPlayerPresent(player -> plugin.getChatManager().sendRawComponent(player, component, vars));
    }

    public void sendActionBar(String path, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendActionBar(player, path, variables));
    }

    public void sendRawActionBar(String msg, Var... variables) {
        ifPlayerPresent(player -> plugin.getChatManager().sendRawActionBar(player, msg, variables));
    }

    public void sendTitleComponent(Component title, Component subtitle, int fadeIn, int stay, int fadeOut, Var... vars) {
        ifPlayerPresent(player -> plugin.getChatManager().sendRawTitleComponent(player, title, subtitle, fadeIn, stay, fadeOut, vars));
    }

    public void sendTitle(String titlePath, String subtitlePath, int stay, Var... vars) {
        sendTitle(titlePath, subtitlePath, 0, stay, 0, vars);
    }

    public void sendTitle(String titlePath, String subtitlePath, int fadeIn, int stay, int fadeOut, Var... vars) {
        ifPlayerPresent(player -> plugin.getChatManager().sendTitle(player, titlePath, subtitlePath, fadeIn, stay, fadeOut, vars));
    }

    public void sendRawTitle(String title, String subtitle, int stay) {
        sendRawTitle(title, subtitle, 0, stay, 0);
    }

    public void sendRawTitle(String rawTitle, String rawSubtitle, int fadeIn, int stay, int fadeOut) {
        ifPlayerPresent(player -> {
            plugin.getChatManager().sendRawTitle(player, rawTitle, rawSubtitle, fadeIn, stay, fadeOut);
        });
    }

    public Arena getArena() {
        return plugin.getArenaRegistry().getArena(this);
    }

    public boolean isInArena() {
        return getArena() != null;
    }

    public boolean hasCooldown(String cooldownName) {
        return getCooldown(cooldownName) > 0D;
    }

    public void setCooldown(String cooldownName, double seconds) {
        cooldowns.put(cooldownName, System.currentTimeMillis() + Math.round(seconds * 1000D));
    }

    public void removeCooldown(String cooldownName) {
        cooldowns.remove(cooldownName);
    }

    public double getCooldown(String cooldownName) {
        Long expiresAt = cooldowns.get(cooldownName);
        if (expiresAt == null) {
            return 0D;
        }

        long remainingMillis = expiresAt - System.currentTimeMillis();
        if (remainingMillis <= 0L) {
            cooldowns.remove(cooldownName);
            return 0D;
        }

        return remainingMillis / 1000D;
    }

    @SuppressWarnings("unchecked")
    public <T> T getStatistic(StatisticType<T> type) {
        return (T) stats.computeIfAbsent(type, stat -> {
            if (stat.getDefaultValue() instanceof Map) {
                return new HashMap<>();
            }

            return stat.getDefaultValue();
        });
    }

    public <T> void setStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, true);
    }

    public <T> void loadStatistic(StatisticType<T> type, T newValue) {
        setStatisticInternal(type, newValue, false);
    }

    private <T> void setStatisticInternal(StatisticType<T> type, T newValue, boolean callEvent) {
        T oldValue = getStatistic(type);
        if (oldValue != null && oldValue.equals(newValue)) {
            return;
        }

        T finalValue = newValue;

        if (callEvent) {
            PlayerStatisticChangeEvent<T> event =
                plugin.getEventManager().statChange(getPlayer(), type, oldValue, newValue);

            if (event.isCancelled()) {
                return;
            }

            finalValue = event.getNewValue();
        }

        if (oldValue != null && oldValue.equals(finalValue)) {
            return;
        }

        stats.put(type, finalValue);
    }

    public void addStat(StatisticType<Integer> type, int amount) {
        setStatistic(type, getStatistic(type) + amount);
    }

    @SafeVarargs
    public final void addStat(StatisticType<Integer> type, StatisticType<Integer>... types) {
        addStat(type, 1);

        for (StatisticType<Integer> statisticType : types) {
            addStat(statisticType, 1);
        }
    }

    public void setStatisticIfHigher(StatisticType<Integer> type, int amount) {
        setStatistic(type, Math.max(getStatistic(type), amount));
    }

    public void resetTemporaryStats() {
        cooldowns.clear();

        for (StatisticType<?> stat : Statistics.getTemporaryStats()) {
            stats.put(stat, stat.getDefaultValue());
        }
    }

    public long getArenaBestTime(String arenaId) {
        Map<String, Long> times = getStatistic(Statistics.ARENA_BEST_TIMES);
        return times.getOrDefault(arenaId, -1L);
    }

    public void setArenaBestTime(String arenaId, long time) {
        Map<String, Long> times = getStatistic(Statistics.ARENA_BEST_TIMES);
        times.put(arenaId, time);

        setStatistic(Statistics.ARENA_BEST_TIMES, times);
    }

    public void resetArenaStats(String arenaId) {
        Map<String, Long> arenaTimes = new HashMap<>(getStatistic(Statistics.ARENA_BEST_TIMES));

        boolean changed = arenaTimes.remove(arenaId) != null;

        if (!changed) {
            return;
        }

        setStatistic(Statistics.ARENA_BEST_TIMES, arenaTimes);
    }
}

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

package dev.despical.tntrun.utils;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 13.07.2023
 */
public class Utils {

    private static final Main plugin = JavaPlugin.getPlugin(Main.class);

    private Utils() {
    }

    public static void applyActionBarCooldown(final User user, int seconds) {
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = seconds * 20;

            @Override
            public void run() {
                var arena = user.getArena();

                if (arena == null || arena.isDeathPlayer(user) || !arena.isState(GameState.IN_GAME)) {
                    cancel();
                    return;
                }

                if (ticks >= maxTicks || !user.hasCooldown("double_jump")) {
                    user.removeCooldown("double_jump");
                    user.sendRawActionBarComponent(Component.empty());
                    restoreDoubleJumpFlight(user);
                    cancel();
                    return;
                }

                var progress = getProgressBar(ticks, maxTicks);
                double remaining = Math.max(0.1D, (maxTicks - ticks) / 20D);
                user.sendRawActionBar(plugin.getChatManager().message("cooldown-format", user)
                    .replace("%progress%", progress)
                    .replace("%time%", String.format(java.util.Locale.US, "%.1f", remaining)));

                ticks += 2;
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    public static void restoreDoubleJumpFlightWhenReady(User user, int seconds) {
        Schedulers.runTaskLater(() -> {
            if (!user.hasCooldown("double_jump")) {
                restoreDoubleJumpFlight(user);
            }
        }, seconds * 20L);
    }

    private static void restoreDoubleJumpFlight(User user) {
        var arena = user.getArena();
        Player player = user.getPlayer();

        if (player == null || arena == null || arena.isDeathPlayer(user) || !arena.isState(GameState.IN_GAME)) {
            return;
        }

        if (user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS) > 0) {
            player.setAllowFlight(true);
        }
    }

    private static String getProgressBar(int current, int max) {
        float percent = (float) current / max;
        int progressBars = (int) (10 * percent), leftOver = (10 - progressBars);
        String[] colors = plugin.getChatManager().message("cooldown-progress-format").split(":");

        return "%s%s%s%s".formatted(colors[0], colors[2].repeat(Math.max(0, progressBars)), colors[1], colors[2].repeat(Math.max(0, leftOver)));
    }

    public static String NONE = getMessage("none");

    public static String getRawString(String string) {
        return plugin.getConfig().getString(string, "&cThe value inside the path is null. (path: " + string + ")");
    }

    public static String getRawString(FileConfiguration config, String string) {
        return config.getString(string);
    }

    public static List<String> getStringList(String path) {
        return plugin.getConfig().getStringList(path);
    }

    public static String format(String string, Var... variables) {
        for (Var variable : variables) {
            string = string.replace(variable.name, variable.value.toString());
        }

        return string;
    }

    public static String getString(String path) {
        if (plugin.getConfig().isList(path)) {
            return getListAsString(path);
        }

        return getRawString(path);
    }

    public static String getString(FileConfiguration file, String path) {
        if (file.isList(path)) {
            return getListAsString(file, path);
        }

        return getRawString(file, path);
    }

    public static String getMessage(String path, Var... variables) {
        return plugin.getChatManager().getRawString(path, variables);
    }

    public static List<String> getStringList(FileConfiguration config, String string) {
        return config.getStringList(string);
    }

    public static String getListAsString(String path) {
        return listToString(getStringList(path));
    }

    public static String getListAsString(FileConfiguration file, String path) {
        return listToString(getStringList(file, path));
    }

    public static String listToString(List<String> list) {
        return String.join("\n", list);
    }

    public static String formatTime(long seconds) {
        return "%02d:%02d".formatted(seconds / 60, seconds % 60);
    }

    public static void resetPlayerAttributes(Player player) {
        player.setHealth(20D);
        player.setFoodLevel(20);
        player.setExp(0F);
        player.setExhaustion(0F);
        player.setSaturation(20F);
        player.setFireTicks(0);
        player.setFlying(false);
        player.setItemOnCursor(null);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
        player.setGameMode(GameMode.ADVENTURE);
    }
}

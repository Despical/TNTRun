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

import dev.despical.tntrun.TNTRun;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class Schedulers {

    private static final TNTRun plugin = TNTRun.getInstance();
    private static final BukkitScheduler scheduler = Bukkit.getScheduler();

    private Schedulers() {
    }

    public static void runInTheNextTick(Runnable runnable) {
        scheduler.runTask(plugin, runnable);
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        scheduler.runTaskLater(plugin, runnable, delay);
    }

    public static void runTaskAsynchronously(Runnable runnable) {
        scheduler.runTaskAsynchronously(plugin, runnable);
    }
}

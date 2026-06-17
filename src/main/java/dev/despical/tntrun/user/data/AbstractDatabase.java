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

package dev.despical.tntrun.user.data;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.api.statistic.StatisticType;
import dev.despical.tntrun.user.User;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public abstract sealed class AbstractDatabase permits FlatFileStatistics, MySQLStatistics {

    protected static final Main plugin = JavaPlugin.getPlugin(Main.class);

    public abstract void saveStatistic(User user, StatisticType statisticType);

    public abstract void saveStatistics(User user);

    public abstract void loadStatistics(User user);

    public abstract void saveAllStatistics();

    public abstract void shutdown();
}

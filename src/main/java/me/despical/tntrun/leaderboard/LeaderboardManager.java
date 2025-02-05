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

package me.despical.tntrun.leaderboard;

import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.user.data.AbstractDatabase;
import me.despical.tntrun.user.data.MySQLStatistics;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 1.11.2024
 */
public class LeaderboardManager {

    private final Main plugin;
    private final Map<StatisticType, Leaderboard> leaderboards;

    public LeaderboardManager(Main plugin) {
        this.plugin = plugin;
        this.leaderboards = new EnumMap<>(StatisticType.class);
    }

    public Map.Entry<UUID, Integer> getEntry(StatisticType type, int placement) {
        return leaderboards.get(type).getEntry(placement);
    }

    public void updateLeaderboards() {
        for (StatisticType type : StatisticType.values()) {
            this.leaderboards.put(type, this.getLeaderboard(type));
        }

        this.leaderboards.values().forEach(Leaderboard::sort);
    }

    private Leaderboard getLeaderboard(StatisticType stat) {
        Leaderboard leaderboard = new Leaderboard();
        AbstractDatabase database = plugin.getUserManager().getUserDatabase();

        if (database instanceof MySQLStatistics) {
            MySQLStatistics mySQLManager = (MySQLStatistics) database;

            try (Connection connection = mySQLManager.getDatabase().getConnection()) {
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery(String.format("SELECT UUID, %s FROM %s ORDER BY %s", stat.getName(), mySQLManager.getTableName(), stat.getName()));

                while (set.next()) {
                    leaderboard.addEntry(UUID.fromString(set.getString("UUID")), set.getInt(stat.getName()));
                }

                return leaderboard;
            } catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        }

        FileConfiguration config = ConfigUtils.getConfig(plugin, "stats");

        for (String uuid : config.getKeys(false)) {
            leaderboard.addEntry(UUID.fromString(uuid), config.getInt(uuid + "." + stat.getName()));
        }

        return leaderboard;
    }
}

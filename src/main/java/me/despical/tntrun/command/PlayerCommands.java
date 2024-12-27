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

package me.despical.tntrun.command;

import me.despical.commandframework.CommandArguments;
import me.despical.commandframework.annotations.Command;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.string.StringUtils;
import me.despical.tntrun.ConfigPreferences;
import me.despical.tntrun.api.statistic.StatisticType;
import me.despical.tntrun.api.statistic.StatsStorage;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;
import me.despical.tntrun.user.data.MySQLStatistics;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Despical
 * <p>
 * Created at 4.02.2023
 */
public class PlayerCommands extends AbstractCommand {

    @Command(
        name = "tntrun.join",
        usage = "/tntrun join <arena>",
        senderType = Command.SenderType.PLAYER
    )
    public void joinCommand(User user, CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            user.sendMessage("admin-commands.provide-an-arena-name");
            return;
        }

        Arena arena = plugin.getArenaRegistry().getArena(arguments.getArgument(0));

        if (arena == null) {
            user.sendMessage("admin-commands.no-arena-found-with-that-name");
            return;
        }

        plugin.getArenaManager().joinAttempt(user, arena);
    }

    @Command(
        name = "tntrun.randomjoin",
        usage = "/tntrun randomjoin",
        senderType = Command.SenderType.PLAYER
    )
    public void randomJoinCommand(User user) {
        if (plugin.getOption(ConfigPreferences.Option.BUNGEE_ENABLED)) {
            user.sendMessage("player-commands.no-random-join-for-bungee");
            return;
        }

        var arenas = plugin.getArenaRegistry().getArenas()
            .stream()
            .filter(arena -> arena.isArenaState(ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING))
            .filter(arena -> arena.getPlayers().size() < arena.getMaximumPlayers())
            .sorted(Comparator.comparingInt(arena -> arena.getPlayers().size()))
            .toList();

        if (!arenas.isEmpty()) {
            int index = arenas.stream().allMatch(arena -> arena.getPlayers().isEmpty()) ? ThreadLocalRandom.current().nextInt(arenas.size()) : arenas.size() - 1;
            Arena arena = arenas.get(index);

            plugin.getArenaManager().joinAttempt(user, arena);
            return;
        }

        user.sendMessage("player-commands.no-free-arenas");
    }

    @Command(
        name = "tntrun.leave",
        usage = "/tntrun leave",
        senderType = Command.SenderType.PLAYER
    )
    public void leaveCommand(User user) {
        Arena arena = user.getArena();

        if (arena == null) {
            user.sendMessage("messages.arena.not-playing");
            return;
        }

        plugin.getArenaManager().leaveAttempt(user, arena);
    }

    @Command(
        name = "tntrun.stats",
        usage = "/tntrun stats [player]",
        senderType = Command.SenderType.PLAYER
    )
    public void statsCommand(User user, CommandArguments arguments) {
        Player sender = arguments.getSender();

        if (arguments.isArgumentsEmpty()) {
            chatManager.getStringList("player-commands.stats-command.messages").stream().map(message -> formatStats(message, true, user)).forEach(user::sendRawMessage);
            return;
        }

        arguments.getPlayer(0).ifPresentOrElse(player -> {
            var targetUser = plugin.getUserManager().getUser(player);
            var self = sender.equals(player);

            chatManager.getStringList("player-commands.stats-command.messages").stream().map(message -> formatStats(message, self, targetUser)).forEach(user::sendRawMessage);
        }, () -> arguments.sendMessage(chatManager.message("player-commands.no-player-found")));
    }

    private String formatStats(String message, boolean self, User user) {
        message = message.replace("%header%", chatManager.message("player-commands.stats-command.header" + (self ? "" : "-other")));
        message = message.replace("%player%", user.getName());
        message = message.replace("%coins%", StatisticType.COINS.from(user));
        message = message.replace("%longest_survive%", StringFormatUtils.formatIntoMMSS(user.getStat(StatisticType.LONGEST_SURVIVE)));
        message = message.replace("%games_played%", StatisticType.GAMES_PLAYED.from(user));
        message = message.replace("%wins%", StatisticType.WINS.from(user));
        message = message.replace("%loses%", StatisticType.LOSES.from(user));
        return chatManager.rawMessage(message);
    }

    @Command(
        name = "tntrun.top",
        usage = "/tntrun top <statistic name>"
    )
    public void leaderboardCommand(CommandArguments arguments) {
        if (arguments.isArgumentsEmpty()) {
            arguments.sendMessage(chatManager.message("player-commands.statistics.type-name"));
            return;
        }

        try {
            printLeaderboard(arguments.getSender(), StatisticType.valueOf(arguments.getArgument(0).toUpperCase()));
        } catch (IllegalArgumentException exception) {
            arguments.sendMessage(chatManager.message("player-commands.statistics.invalid-name"));
        }
    }

    private void printLeaderboard(CommandSender sender, StatisticType statisticType) {
        sender.sendMessage(chatManager.message("player-commands.statistics.header"));

        var stats = StatsStorage.getStats(statisticType);
        String statistic = StringUtils.capitalize(statisticType.name().toLowerCase().replace("_", " "));

        for (int i = 0; i < 10; i++) {
            try {
                UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];
                sender.sendMessage(formatMessage(statistic, plugin.getServer().getOfflinePlayer(current).getName(), i + 1, stats.get(current)));
                stats.remove(current);
            } catch (IndexOutOfBoundsException ex) {
                sender.sendMessage(formatMessage(statistic, "Empty", i + 1, 0));
            } catch (NullPointerException ex) {
                UUID current = (UUID) stats.keySet().toArray()[stats.keySet().toArray().length - 1];

                if (plugin.getUserManager().getUserDatabase() instanceof MySQLStatistics mySQLManager) {
                    try (Connection connection = mySQLManager.getDatabase().getConnection()) {
                        Statement statement = connection.createStatement();
                        ResultSet set = statement.executeQuery("SELECT name FROM %s WHERE UUID='%s'".formatted(mySQLManager.getTableName(), current.toString()));

                        if (set.next()) {
                            sender.sendMessage(formatMessage(statistic, set.getString(1), i + 1, stats.get(current)));
                            continue;
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                }

                sender.sendMessage(formatMessage(statistic, "Unknown Player", i + 1, stats.get(current)));
            }
        }
    }

    private String formatMessage(String statisticName, String playerName, int position, int value) {
        String message = chatManager.message("player-commands.statistics.format");

        message = message.replace("%position%", Integer.toString(position));
        message = message.replace("%name%", playerName);
        message = message.replace("%value%", statisticName.startsWith("Longest") ? StringFormatUtils.formatIntoMMSS(value) : Integer.toString(value));
        message = message.replace("%statistic%", statisticName);
        return message;
    }
}

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

package me.despical.tntrun.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.commons.string.StringFormatUtils;
import me.despical.commons.util.Strings;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.user.User;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Despical
 * <p>
 * Created at 10.07.2020
 */
public class ChatManager {

    private final Main plugin;
    private FileConfiguration config;

    public ChatManager(Main plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public String message(String path) {
        return rawMessage(this.config.getString(path));
    }

    public String message(String path, User user) {
        String message = this.message(path);

        message = message.replace("%player%", user.getName());

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(user.getPlayer(), message);
        }

        return message;
    }

    public String message(String path, Arena arena, User user) {
        String message = this.message(path, arena);

        message = message.replace("%player%", user.getName());

        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            message = PlaceholderAPI.setPlaceholders(user.getPlayer(), message);
        }

        return message;
    }

    public String message(String path, Arena arena) {
        String message = this.message(path);

        return formatMessage(arena, message);
    }

    public String formatMessage(Arena arena, String message) {
        message = message.replace("%map%", arena.getMapName());
        message = message.replace("%time%", Integer.toString(arena.getTimer()));
        message = message.replace("%formatted_time%", StringFormatUtils.formatIntoMMSS(arena.getTimer()));
        message = message.replace("%players%", Integer.toString(arena.getPlayers().size()));
        message = message.replace("%players_left%", Integer.toString(arena.getPlayersLeft().size()));
        message = message.replace("%state%", arena.getArenaState().getFormattedName());
        message = message.replace("%min_players%", Integer.toString(arena.getMinimumPlayers()));
        message = message.replace("%max_players%", Integer.toString(arena.getMaximumPlayers()));
        return message;
    }

    public String rawMessage(String message) {
        return Strings.format(message);
    }

    public List<String> getStringList(String path) {
        return this.config.getStringList(path);
    }

    public void reload() {
        this.config = ConfigUtils.getConfig(plugin, "messages");

        StringFormatUtils.setTimeFormat(this.message("Scoreboard.Timer-Format"));
        StringFormatUtils.setDateFormat(this.message("Scoreboard.Date-Format"));

        Stream.of(ArenaState.values()).forEach(arenaState -> arenaState.setFormattedName(this.message(arenaState.getPath())));
    }
}

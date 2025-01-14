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

package me.despical.tntrun.handlers.bungee;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.despical.commons.configuration.ConfigUtils;
import me.despical.tntrun.Main;
import me.despical.tntrun.arena.Arena;
import me.despical.tntrun.arena.ArenaState;
import me.despical.tntrun.events.EventListener;
import me.despical.tntrun.user.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 23.06.2023
 */
public class BungeeManager extends EventListener {

    private final String motd, hubName;
    private final boolean motdEnabled, connectToHub;
    private final Map<ArenaState, String> gameStates;

    public BungeeManager(Main plugin) {
        super(plugin);
        this.gameStates = new EnumMap<>(ArenaState.class);

        final var config = ConfigUtils.getConfig(plugin, "bungee");

        this.motd = plugin.getChatManager().rawMessage(config.getString("MOTD.Message"));
        this.hubName = config.getString("Hub");
        this.motdEnabled = config.getBoolean("MOTD.Enabled");
        this.connectToHub = config.getBoolean("Connect-To-Hub");

        for (ArenaState state : ArenaState.values()) {
            gameStates.put(state, plugin.getChatManager().rawMessage(config.getString("MOTD.Game-States." + state.getDefaultName())));
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public boolean connectToHub(User user) {
        if (!plugin.isEnabled() || !connectToHub) return false;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(hubName);

        user.getPlayer().sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        return true;
    }

    @EventHandler
    public void onServerListPing(ServerListPingEvent event) {
        if (!motdEnabled) return;

        var arenaRegistry = plugin.getArenaRegistry();

        if (arenaRegistry.getArenas().isEmpty()) return;

        Arena bungeeArena = arenaRegistry.getBungeeArena();

        event.setMaxPlayers(bungeeArena.getMaximumPlayers());
        event.setMotd(motd.replace("%state%", gameStates.get(bungeeArena.getArenaState())));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var arenaRegistry = plugin.getArenaRegistry();

        if (arenaRegistry.getArenas().isEmpty()) return;

        event.setJoinMessage("");
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.getArenaManager().joinAttempt(plugin.getUserManager().getUser(event.getPlayer()), arenaRegistry.getBungeeArena()), 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var arenaRegistry = plugin.getArenaRegistry();

        if (arenaRegistry.getArenas().isEmpty()) return;

        event.setQuitMessage("");

        User user = plugin.getUserManager().getUser(event.getPlayer());

        if (user.isInArena()) {
            plugin.getArenaManager().leaveAttempt(user, arenaRegistry.getBungeeArena());
        }
    }
}

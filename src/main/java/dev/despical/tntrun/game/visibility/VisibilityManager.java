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

package dev.despical.tntrun.game.visibility;

import dev.despical.tntrun.TNTRun;
import dev.despical.tntrun.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class VisibilityManager {

    private static final TNTRun plugin = TNTRun.getInstance();

    private final Game game;

    public VisibilityManager(Game game) {
        this.game = game;
    }

    public void hidePlayersFromEachOther() {
        for (Player player : game.getPlayers()) {
            for (Player other : game.getPlayers()) {
                if (player.equals(other)) continue;

                hide(player, other);
            }
        }
    }

    public void showPlayersToEachOther() {
        for (Player player : game.getPlayers()) {
            for (Player other : game.getPlayers()) {
                if (player.equals(other)) continue;

                show(player, other);
            }
        }
    }

    public void hidePlayerToInGamePlayers(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            hide(inGamePlayer, player);
            show(player, inGamePlayer);
        }
    }

    public void hidePlayerFromGame(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            hide(inGamePlayer, player);
            hide(player, inGamePlayer);
        }
    }

    public void hidePlayerToOutsideGame(Player player) {
        for (Player outsidePlayer : getOthers()) {
            hide(outsidePlayer, player);
            hide(player, outsidePlayer);
        }
    }

    public void showPlayerToInGamePlayers(Player player) {
        for (Player inGamePlayer : game.getPlayers()) {
            show(inGamePlayer, player);
            show(player, inGamePlayer);
        }
    }

    public void showPlayerOutsideTheGame(Player player) {
        var arenaRegistry = plugin.getArenaRegistry();

        getOthers()
            .stream()
            .filter(Predicate.not(arenaRegistry::isInArena))
            .forEach(outsidePlayer -> {
                show(player, outsidePlayer);
                show(outsidePlayer, player);
            });
    }

    private void hide(Player player, Player from) {
        player.hidePlayer(plugin, from);
    }

    private void show(Player player, Player from) {
        player.showPlayer(plugin, from);
    }

    private List<Player> getOthers() {
        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        onlinePlayers.removeAll(game.getPlayers());
        return onlinePlayers;
    }
}

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

package dev.despical.tntrun.game.states;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.option.BooleanOption;
import dev.despical.tntrun.option.IntOption;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Utils;
import dev.despical.tntrun.utils.Var;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
public class WaitingState extends GameStateHandler {

    private int oldTimer;

    public WaitingState(Game game) {
        super(game);
    }

    @Override
    public void tick() {
        List<User> players = game.getUsers();
        if (players.isEmpty()) {
            return;
        }

        int timer = game.getTimer();

        if (timer > 0) {
            game.setTimer(--timer);
            handleLevelBarTimer(timer);

            if (timer % 60 == 0) {
                broadcastWaitingMessageIfNeeded();
            }

            return;
        }

        game.setTimer(IntOption.LOBBY_WAITING_TIME.value());
    }

    @Override
    public void firstTick() {
        if (oldTimer != 0) {
            game.setTimer(oldTimer);

            oldTimer = 0;
            return;
        }

        game.setTimer(IntOption.LOBBY_WAITING_TIME.value());
    }

    @Override
    public void join(User user) {
        handlePlayerJoin(user);
        handleTimerOnPlayerJoin();
    }

    public void handlePlayerJoin(User user) {
        Player player = user.getPlayer();
        player.teleport(getLocation(ArenaKeys.LOBBY_LOCATION));

        visibilityManager.hidePlayerToOutsideGame(player);
        visibilityManager.showPlayerToInGamePlayers(player);

        game.getScoreboardManager().createScoreboard(player);
        game.getBossBarManager().addPlayer(player);

        int playerAmount = game.getUsers().size();
        int maxPlayerAmount = arena.getOption(ArenaKeys.MAX_PLAYERS);
        Var[] vars = {
            Var.of("%player%", player.getDisplayName()),
            Var.of("%players%", playerAmount),
            Var.of("%max_players%", maxPlayerAmount)
        };

        game.broadcastMessage("player-joined", vars);
        chatManager.sendCenteredMessage(player, "game-explanation",
            Var.of("%map_name%", arena.getOption(ArenaKeys.MAP_NAME)),
            Var.of("%map_author%", arena.getOption(ArenaKeys.MAP_AUTHOR))
        );

        handleInventory(player);
        resetPlayerAttributes(player);
    }

    private void handleTimerOnPlayerJoin() {
        int playerAmount = game.getUsers().size();
        int minPlayerAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int preGameWaitingTime = IntOption.PRE_GAME_WAITING_TIME.value();

        if (playerAmount == minPlayerAmount) {
            if (game.getTimer() > preGameWaitingTime) {
                oldTimer = game.getTimer();
            }

            game.setGameState(GameState.STARTING);

            int maxPlayerAmount = arena.getOption(ArenaKeys.MAX_PLAYERS);
            int fullGameStartingTime = IntOption.FULL_GAME_STARTING_TIME.value();

            if (playerAmount >= maxPlayerAmount && game.getTimer() > fullGameStartingTime) {
                game.setTimer(fullGameStartingTime);
            }
        }
    }

    @Override
    public void leave(User user) {
        game.broadcastMessage("player-left", Var.ofPlayer(user));

        handleTimerOnLeave();
    }

    private void handleTimerOnLeave() {
        int playerAmount = game.getUsers().size();
        int minPlayerAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int preGameWaitingTime = IntOption.PRE_GAME_WAITING_TIME.value();

        if (playerAmount < minPlayerAmount && game.getTimer() <= preGameWaitingTime) {
            game.setTimer(oldTimer);
        }
    }

    @Override
    public void resetPlayerAttributes(Player player) {
        Utils.resetPlayerAttributes(player);

        player.setLevel(BooleanOption.LEVEL_BAR_TIMER.value() ? game.getTimer() : 0);
        player.setInvulnerable(false);
        player.clearTitle();
        player.sendActionBar(Component.empty());
    }

    private void handleInventory(Player player) {
        saveAndClearInventory(player);
        giveLobbyItems(player);

        User user = plugin.getUserManager().getUser(player);
        int doubleJumps = Statistics.getDoubleJumps(player);

        user.setStatistic(Statistics.LOCAL_DOUBLE_JUMPS, doubleJumps);
        user.setStatistic(Statistics.LOCAL_MAX_DOUBLE_JUMPS, doubleJumps);

        game.updatePlayerMetadata(user);
    }

    private void giveLobbyItems(Player player) {
        itemManager.getItem("leave-item").giveTo(player, "slot");
    }

    private void broadcastWaitingMessageIfNeeded() {
        int playerAmount = game.getUsers().size();
        int minAmount = arena.getOption(ArenaKeys.MIN_PLAYERS);
        int playersNeeded = Math.max(0, minAmount - playerAmount);

        if (playersNeeded == 0) {
            return;
        }

        boolean singular = playersNeeded == 1;
        game.broadcastMessage("waiting-for-players",
            Var.of("%s%", singular ? "" : "s"),
            Var.of("%to_be_form%", singular ? "is" : "are"),
            Var.of("%players_needed%", playersNeeded)
        );
    }
}

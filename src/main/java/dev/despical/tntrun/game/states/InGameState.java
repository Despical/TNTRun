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
import dev.despical.tntrun.game.ArenaPotionEffect;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.PotionUtils;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Despical
 * <p>
 * Created at 29.12.2025
 */
public class InGameState extends GameStateHandler {

    public InGameState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        Location startLocation = game.getStartLocation();

        game.getPlayers().forEach(player -> {
            player.getInventory().clear();
            player.teleport(startLocation);

            giveDoubleJumpItem(player);
            applyArenaPotionEffects(player);
        });

        game.getScoreboardManager().updateNameTagsVisibility();
        game.getScores().resetScores();
        game.startSurvivalRound();
        game.setTimer(0);
        game.getBlockRemovalManager().start();
        game.broadcastMessage("game-started");

        plugin.getEventManager().gameStart(game);
    }

    private void giveDoubleJumpItem(Player player) {
        var doubleJumpItem = itemManager.getItem("double-jump");
        if (doubleJumpItem != null) {
            doubleJumpItem.giveTo(player, "slot");
        }
    }

    @Override
    public void tick() {
        int timer = game.getTimer();
        game.setTimer(++timer);

        for (User user : game.getUsers()) {
            if (user.isSpectator() || arena.isDeathPlayer(user)) {
                continue;
            }

            Player player = user.getPlayer();
            if (player == null) {
                continue;
            }

            if (user.getCooldown("double_jump") > 0) {
                player.setAllowFlight(false);
            } else if (user.getStatistic(Statistics.LOCAL_DOUBLE_JUMPS) > 0) {
                player.setAllowFlight(true);
            }

            user.addStat(Statistics.LOCAL_SURVIVE_TIME, 1);
            game.getScores().addScore(user, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
        }
    }

    private void applyArenaPotionEffects(Player player) {
        for (ArenaPotionEffect arenaEffect : arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS)) {
            PotionEffectType type = arenaEffect.getPotionEffectType();

            if (type == null) {
                continue;
            }

            PotionUtils.applyInfinite(player, type, arenaEffect.getLevel());
        }
    }

    @Override
    public void join(User user) {
        game.prepareSpectator(user, true);
    }

    @Override
    public void leave(User user) {
        boolean wasSpectator = user.isSpectator();
        game.getScores().addScore(user, user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));

        if (!wasSpectator) {
            game.broadcastMessage("disconnected-from-the-game", Var.ofPlayer(user));
        }

        if (game.getUsers().isEmpty()) {
            game.setGameState(GameState.RESTARTING);
            return;
        }

        if (!wasSpectator) {
            game.finishIfLastSurvivor();
        }
    }
}

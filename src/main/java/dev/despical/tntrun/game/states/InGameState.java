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
        game.startBlockRemoving();
        game.broadcastMessage("game-started");

        plugin.getEventManager().gameStart(game);
    }

    private void giveDoubleJumpItem(Player player) {
        var doubleJumpItem = plugin.getItemManager().getItem("double-jump");
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
            game.getScores().addScore(user.getUUID(), user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
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
        game.getScores().addScore(user.getUUID(), user.getStatistic(Statistics.LOCAL_SURVIVE_TIME));
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

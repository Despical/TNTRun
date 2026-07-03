package dev.despical.tntrun.game.states;

import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.ArenaPotionEffect;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.game.GameState;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.Var;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 29.12.2025
 */
public class InGameState extends GameStateHandler {

    private static final int SURVIVE_TIME_REWARD = 30;

    public InGameState(Game game) {
        super(game);
    }

    @Override
    public void firstTick() {
        Location startLocation = game.getStartLocation();

        game.getPlayers().forEach(player -> {
            player.getInventory().clear();
            player.teleport(startLocation);
            applyArenaPotionEffects(player);
        });

        game.getScoreboardManager().updateNameTagsVisibility();
        game.getScores().resetScores();
        game.setTimer(0);
        game.startBlockRemoving();

        plugin.getEventManager().gameStart(game);
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

            if (timer > 0 && timer % 30 == 0) {
                rewardSurviveTime(user);
                game.getScores().addScore(user.getUUID(), user.getStatistic(Statistics.LOCAL_COIN));
            }
        }
    }

    private void rewardSurviveTime(User user) {
        String score = (SURVIVE_TIME_REWARD > 0 ? "+" : "") + SURVIVE_TIME_REWARD;
        String action = plugin.getChatManager().message("messages.score-actions.survive");
        String message = plugin.getChatManager().message("messages.score-actions.bonus-score",
            Var.of("%score%", score),
            Var.of("%action%", action)
        );

        user.addStat(Statistics.LOCAL_COIN, SURVIVE_TIME_REWARD);
        user.sendRawActionBar(message.replace(action, "").trim());
        user.sendRawMessage(message);
    }

    private void applyArenaPotionEffects(Player player) {
        for (ArenaPotionEffect arenaEffect : arena.getOption(ArenaKeys.ARENA_POTION_EFFECTS)) {
            PotionEffectType type = arenaEffect.getPotionEffectType();

            if (type == null) {
                continue;
            }

            int amplifier = Math.max(0, arenaEffect.getLevel() - 1);
            PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false, false);
            player.addPotionEffect(effect);
        }
    }

    @Override
    public void join(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void leave(User user) {
        UUID uuid = user.getUUID();
        game.getScores().removePlayer(uuid);
        game.broadcastMessage("disconnected-from-the-game", Var.ofPlayer(user));

        int playerAmount = game.getUsers().size();
        if (playerAmount == 1) {
            game.setGameState(GameState.ENDING);
        } else if (playerAmount == 0) {
            game.setGameState(GameState.RESTARTING);
        }
    }
}

package dev.despical.tntrun.game.spectator;

import dev.despical.fileitems.SpecialItem;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.stats.Statistics;
import dev.despical.tntrun.user.User;
import dev.despical.tntrun.utils.PotionUtils;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2026
 */
@RequiredArgsConstructor
public class SpectatorManager {

    private static final Main plugin = Main.getInstance();

    private final Game game;

    public void giveItems(Player player) {
        giveCategoryItem(player, "spectator-settings-menu-items", "spectator-settings");
        giveCategoryItem(player, "spectator-teleporter-menu-items", "spectator-teleporter");

        giveItems(player, "play-again", "leave-item");
    }

    public void applySettings(User user) {
        Player player = user.getPlayer();
        if (player == null) {
            return;
        }

        PotionUtils.clearEffects(player);

        int nightVision = user.getStatistic(Statistics.SPECTATOR_NIGHT_VISION_LEVEL);
        if (nightVision == 1) {
            PotionUtils.applyInfinite(player, PotionEffectType.NIGHT_VISION, 1);
        }

        int level = Math.clamp(user.getStatistic(Statistics.SPECTATOR_SPEED), 0, 3);
        player.setFlySpeed(Math.min(1F, .1F + level * .05F));

        if (level > 0) {
            PotionUtils.applyInfinite(player, PotionEffectType.SPEED, level);
        }
    }

    public void updateVisibility() {
        for (User viewerUser : game.getUsers()) {
            Player viewer = viewerUser.getPlayer();
            if (viewer == null) {
                continue;
            }

            boolean viewerShowsSpectators = viewerUser.isSpectator() &&
                viewerUser.getStatistic(Statistics.SPECTATOR_SHOW_OTHERS) == 1;

            for (User targetUser : game.getUsers()) {
                if (viewerUser.equals(targetUser)) {
                    continue;
                }

                Player target = targetUser.getPlayer();
                if (target == null) {
                    continue;
                }

                if (targetUser.isSpectator()) {
                    if (viewerShowsSpectators) {
                        viewer.showPlayer(plugin, target);
                    } else {
                        viewer.hidePlayer(plugin, target);
                    }
                    continue;
                }

                viewer.showPlayer(plugin, target);
            }
        }
    }

    private void giveCategoryItem(Player player, String category, String key) {
        SpecialItem item = plugin.getItemManager().getItemFromCategory(category, key);
        if (item != null) {
            item.giveTo(player, "slot");
        }
    }

    private void giveItems(Player player, String... keys) {
        for (String key : keys) {
            giveItem(player, key);
        }
    }

    private void giveItem(Player player, String key) {
        SpecialItem item = plugin.getItemManager().getItem(key);

        if (item != null) {
            item.giveTo(player, "slot");
        }
    }
}

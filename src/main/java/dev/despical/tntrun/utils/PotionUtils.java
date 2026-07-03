package dev.despical.tntrun.utils;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Despical
 * <p>
 * Created at 3.07.2026
 */
public final class PotionUtils {

    private PotionUtils() {
    }

    public static void applyInfinite(Player player, PotionEffectType type, int level) {
        if (level <= 0) {
            remove(player, type);
            return;
        }

        PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, level - 1, false, false, false);
        player.addPotionEffect(effect);
    }

    public static void remove(Player player, PotionEffectType type) {
        player.removePotionEffect(type);
    }

    public static void clearEffects(Player player) {
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    }
}

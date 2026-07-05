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

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

package dev.despical.tntrun.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@Getter
@Setter
@AllArgsConstructor
public class ArenaPotionEffect {

    private static final Map<String, String> EFFECT_ALIASES = Map.ofEntries(
        Map.entry("JUMP_BOOST", "JUMP"),
        Map.entry("HASTE", "FAST_DIGGING"),
        Map.entry("MINING_FATIGUE", "SLOW_DIGGING"),
        Map.entry("STRENGTH", "INCREASE_DAMAGE"),
        Map.entry("INSTANT_HEALTH", "HEAL"),
        Map.entry("INSTANT_DAMAGE", "HARM"),
        Map.entry("RESISTANCE", "DAMAGE_RESISTANCE")
    );

    private String effectType;
    private int level;

    public PotionEffectType getPotionEffectType() {
        try {
            String normalized = effectType.toUpperCase(Locale.ENGLISH);
            PotionEffectType effect = PotionEffectType.getByName(normalized);

            if (effect != null) {
                return effect;
            }

            return PotionEffectType.getByName(EFFECT_ALIASES.getOrDefault(normalized, normalized));
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String toString() {
        return effectType + ":" + level;
    }

    public static ArenaPotionEffect fromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }

        String[] parts = str.split(":");
        if (parts.length != 2) {
            return null;
        }

        try {
            return new ArenaPotionEffect(parts[0], Integer.parseInt(parts[1]));
        } catch (NumberFormatException _) {
            return null;
        }
    }
}

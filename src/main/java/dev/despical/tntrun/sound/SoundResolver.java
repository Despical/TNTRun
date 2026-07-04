package dev.despical.tntrun.sound;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author Despical
 * <p>
 * Created at 4.07.2026
 */
public final class SoundResolver {

    private SoundResolver() {
    }

    public static Sound resolve(String rawSound) {
        if (rawSound == null || rawSound.isBlank()) {
            return null;
        }

        String normalized = normalize(rawSound);
        Sound sound = Registry.SOUNDS.get(toKey(normalized));

        if (sound != null) {
            return sound;
        }

        return resolveConstant(rawSound);
    }

    private static NamespacedKey toKey(String normalized) {
        String key = normalized.contains(":") ? normalized : "minecraft:" + normalized;
        NamespacedKey namespacedKey = NamespacedKey.fromString(key);
        return namespacedKey == null ? NamespacedKey.minecraft(normalized) : namespacedKey;
    }

    private static String normalize(String rawSound) {
        String normalized = rawSound.trim().toLowerCase(Locale.ENGLISH);

        if (normalized.startsWith("minecraft:")) {
            normalized = normalized.substring("minecraft:".length());
        }

        if (normalized.equals("mob.enderdragon.growl")) {
            return "entity.ender_dragon.growl";
        }

        return normalized;
    }

    private static Sound resolveConstant(String rawSound) {
        String fieldName = rawSound.trim()
            .toUpperCase(Locale.ENGLISH)
            .replace('.', '_')
            .replace(':', '_');

        try {
            Field field = Sound.class.getField(fieldName);
            Object value = field.get(null);
            return value instanceof Sound sound ? sound : null;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return null;
        }
    }
}

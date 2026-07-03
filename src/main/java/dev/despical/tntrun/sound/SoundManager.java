package dev.despical.tntrun.sound;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.Main;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 03.07.2026
 */
public class SoundManager {

    private final Main plugin;
    private final Map<GameSound, SoundData> sounds;

    public SoundManager(Main plugin) {
        this.plugin = plugin;
        this.sounds = new EnumMap<>(GameSound.class);
        this.reload();
    }

    public void reload() {
        sounds.clear();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "sounds");

        for (GameSound gameSound : GameSound.values()) {
            String path = "sounds." + gameSound.getPath() + ".";
            if (!config.getBoolean(path + "enabled", true)) {
                continue;
            }

            String soundName = config.getString(path + "sound");
            if (soundName == null || soundName.isBlank()) {
                plugin.getLogger().warning("Could not register sound '" + gameSound.getPath() + "': sound name is empty.");
                continue;
            }

            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning("Could not register sound '" + gameSound.getPath() + "': invalid Bukkit sound name '" + soundName + "'.");
                continue;
            }

            sounds.put(gameSound, new SoundData(
                sound,
                (float) config.getDouble(path + "volume", 1D),
                (float) config.getDouble(path + "pitch", 1D)
            ));
        }
    }

    public void play(Player player, GameSound gameSound) {
        SoundData soundData = sounds.get(gameSound);
        if (soundData == null) {
            return;
        }

        player.playSound(player.getLocation(), soundData.sound(), soundData.volume(), soundData.pitch());
    }

    private record SoundData(Sound sound, float volume, float pitch) {
    }
}

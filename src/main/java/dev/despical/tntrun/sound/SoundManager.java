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

package dev.despical.tntrun.sound;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.TNTRun;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 03.07.2026
 */
public class SoundManager {

    private final TNTRun plugin;
    private final Map<GameSound, SoundData> sounds;

    public SoundManager(TNTRun plugin) {
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

            Sound sound = SoundResolver.resolve(soundName);
            if (sound == null) {
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

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

package dev.despical.tntrun.bossbar;

import dev.despical.commons.configuration.ConfigUtils;
import dev.despical.tntrun.Main;
import dev.despical.tntrun.chat.ChatManager;
import dev.despical.tntrun.game.GameState;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 28.01.2026
 */
public class BossBarConfig {

    @Getter
    private boolean enabled;

    private final Main plugin;
    private final Map<GameState, BossBarData> cache;

    public BossBarConfig(Main plugin) {
        this.plugin = plugin;
        this.cache = new EnumMap<>(GameState.class);
        this.load();
    }

    public void load() {
        cache.clear();

        FileConfiguration config = ConfigUtils.getConfig(plugin, "bossbar");
        this.enabled = config.getBoolean("enabled", true);

        ConfigurationSection section = config.getConfigurationSection("states");
        if (section == null) return;

        ChatManager chatManager = plugin.getChatManager();

        for (String key : section.getKeys(false)) {
            String path = "states." + key + ".";
            String title = config.getString(path + "title", "");

            BossBarData data = new BossBarData(
                chatManager.parseMessage(title),
                BossBar.Color.valueOf(config.getString(path + "color", "PINK")),
                BossBar.Overlay.valueOf(config.getString(path + "overlay", "PROGRESS")),
                !title.isEmpty()
            );

            cache.put(GameState.fromPath(key), data);
        }
    }

    public BossBarData getData(GameState gameState) {
        return cache.get(gameState);
    }

    public record BossBarData(Component title, BossBar.Color color, BossBar.Overlay overlay, boolean visible) {
    }
}

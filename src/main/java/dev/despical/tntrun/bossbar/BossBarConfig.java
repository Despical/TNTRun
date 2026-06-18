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

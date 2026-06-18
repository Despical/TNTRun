package dev.despical.tntrun.bossbar;

import dev.despical.tntrun.Main;
import dev.despical.tntrun.game.Game;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * @author Berke Akçen
 * <p>
 * Created at 28.01.2026
 */
public class BossBarManager {

    private final Game game;
    private final BossBar bossBar;
    private final BossBarConfig configProvider;

    public BossBarManager(Game game) {
        this.game = game;
        this.bossBar = BossBar.bossBar(Component.empty(), 1F, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        this.configProvider = Main.getInstance().getGameManager().getBossBarConfig();
    }

    public void update() {
        if (!configProvider.isEnabled()) {
            removeAll();
            return;
        }

        BossBarConfig.BossBarData data = configProvider.getData(game.getState());
        if (data == null) return;

        if (!data.visible()) {
            removeAll();
            return;
        }

        bossBar.name(data.title());
        bossBar.color(data.color());
        bossBar.overlay(data.overlay());

        showToAll();
    }

    public void showToAll() {
        game.getPlayers().forEach(bossBar::addViewer);
    }

    public void removeAll() {
        game.getPlayers().forEach(bossBar::removeViewer);
    }

    public void addPlayer(Player player) {
        if (configProvider.isEnabled()) {
            bossBar.addViewer(player);
        }
    }

    public void removePlayer(Player player) {
        bossBar.removeViewer(player);
    }
}

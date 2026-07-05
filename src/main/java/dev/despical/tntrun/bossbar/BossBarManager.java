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

import dev.despical.tntrun.Main;
import dev.despical.tntrun.arena.options.ArenaKeys;
import dev.despical.tntrun.game.Game;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * @author Berke Akçen
 * <p>
 * Created at 28.01.2026
 */
/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
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
        if (!isEnabled()) {
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

    private boolean isEnabled() {
        return configProvider.isEnabled() && game.getArena().getOption(ArenaKeys.ARENA_BOSSBAR_ENABLED);
    }
}

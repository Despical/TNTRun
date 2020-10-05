package me.despical.tntrun.events.spectator.components;

import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.tntrun.events.spectator.SpectatorSettingsMenu;

/**
 * @author Despical
 * <p>
 * Created at 05.10.2020
 */
public interface SpectatorSettingComponent {

    void prepare(SpectatorSettingsMenu spectatorSettingsMenu);

    void injectComponents(StaticPane pane);
}
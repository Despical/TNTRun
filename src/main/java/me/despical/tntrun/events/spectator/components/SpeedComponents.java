package me.despical.tntrun.events.spectator.components;

import com.github.despical.inventoryframework.GuiItem;
import com.github.despical.inventoryframework.pane.StaticPane;
import me.despical.commonsbox.compat.XMaterial;
import me.despical.commonsbox.item.ItemBuilder;
import me.despical.tntrun.Main;
import me.despical.tntrun.events.spectator.SpectatorSettingsMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Despical
 * <p>
 * Created at 05.10.2020
 */
public class SpeedComponents implements SpectatorSettingComponent {

    private SpectatorSettingsMenu spectatorSettingsMenu;

    @Override
    public void prepare(SpectatorSettingsMenu spectatorSettingsMenu) {
        this.spectatorSettingsMenu = spectatorSettingsMenu;
    }

    @Override
    public void injectComponents(StaticPane pane) {
        Main plugin = spectatorSettingsMenu.getPlugin();
        Player player = spectatorSettingsMenu.getPlayer();
        String speedPrefix = plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.Speed-Name");

        pane.addItem(new GuiItem(new ItemBuilder(Material.LEATHER_BOOTS)
                .name(plugin.getChatManager().colorMessage("In-Game.Spectator.Settings-Menu.No-Speed"))
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setFlySpeed(0.1f);
        }),2,1);

        pane.addItem(new GuiItem(new ItemBuilder(Material.CHAINMAIL_BOOTS)
                .name(speedPrefix + " I")
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setFlySpeed(0.2f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
        }),3,1);

        pane.addItem(new GuiItem(new ItemBuilder(Material.IRON_BOOTS)
                .name(speedPrefix + " II")
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setFlySpeed(0.25f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, false));
        }),4,1);

        pane.addItem(new GuiItem(new ItemBuilder(XMaterial.GOLDEN_BOOTS.parseMaterial())
                .name(speedPrefix + " III")
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setFlySpeed(0.3f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 3, false, false));
        }),5,1);

        pane.addItem(new GuiItem(new ItemBuilder(Material.DIAMOND_BOOTS)
                .name(speedPrefix + " IV")
                .build(), e -> {
            player.closeInventory();
            player.removePotionEffect(PotionEffectType.SPEED);
            player.setFlySpeed(0.35f);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4, false, false));
        }),6,1);
    }
}
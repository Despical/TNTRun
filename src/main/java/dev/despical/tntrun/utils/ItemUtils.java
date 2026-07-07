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

package dev.despical.tntrun.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.despical.commons.XMaterial;
import dev.despical.commons.reflection.XReflection;
import dev.despical.fileitems.SpecialItem;
import dev.despical.tntrun.TNTRun;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.UUID;

/**
 * @author Despical
 * <p>
 * Created at 18.06.2026
 */
@UtilityClass
public final class ItemUtils {

    public static final ItemStack[] EMPTY_ARMORS = new ItemStack[4];

    private static final TNTRun PLUGIN = TNTRun.getInstance();
    private static final boolean SUPPORTS_1_21_5 = XReflection.of(ItemMeta.class).method("void setHideTooltip(boolean _)").exists();
    private static final UUID OFFLINE_MODE_RESET_HEAD_UUID = UUID.fromString("e57c4a3a-6ec5-4f6b-8bfa-fb287b2a6ed8");
    private static final String OFFLINE_MODE_RESET_HEAD_NAME = "mrdespi.1";
    private static final String OFFLINE_MODE_RESET_HEAD_TEXTURE_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc3ODQ5MjA0NDk1NSwKICAicHJvZmlsZUlkIiA6ICJlNTdjNGEzYTZlYzU0ZjZiOGJmYWZiMjg3YjJhNmVkOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJtcmRlc3BpIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjYTg1YzE1NjNiZjU2OWQ4OGJmN2JjMzc1Y2JjODIwZDdkNGE3M2M5MmFhZjdkOTQ3OWFmNWVmNTI1NWQwNDAiCiAgICB9CiAgfQp9";
    private static final String OFFLINE_MODE_RESET_HEAD_TEXTURE_SIGNATURE = "gJQwuBIayG/9eleAu1NB14dvu2uXrSuSNTlUnFiaZAuDE4vgfGGcyuKTY/u2JPKZGKgr/S8WGvdqNyEUzFRpQf4spWF6u6FW0njE/p5Wcpg7RqxBcnoafgn/mrqllCIOelhI6coWkODkhfBZEEgZmgbD7PVWuJKTDPIyCcc2+ZAebYDQl6dvzOSjrEr5Af55ePil9aQAKBOtAU82joVcwvWcPrYse0UiqQLpo/lLVAQRuJqwJF5C1IjAAA7t9YnaDmfvZOnhI+SIpUuQBtjGJ1sfjH8qiCcv9QrPKRzRqT61NyBJWN/4vp2UMSGpl6NZXg6KaR7pjcX1+12iFoU20UHwsB/LOrnR2CYOX6qZqDj5qPk4kDPftIKqyBl0IIfA2eZ1vEMFkFEcXrdzGGW/0DxmA8aowKp1VLyN2PDXiJ+gPbAibFExHsTNokhJpYuxp0DH1DP6b5jGaLJYfRQr4TvEITvtlI1+S9aOkmHZQ2Lsf02Qrt7vQUXf4z+pGs1lWUsly9nIePQKLRw6CIpjwVejQTmvsQrljPpSe7Qd4+u1hLZcONd0afLB5DWe0b3dtZzUsiEZc/n/j11Jp64MsIIfwwRKgj6WN76JOey387ARjjXLoY3KMVuMiqXkBatUtSy2Yta2JPlr4wzlYSqv0/1jIK2uv3iocRwmfqqbaeE=";

    public static void applyPlayerProfileIfSkull(OfflinePlayer player, ItemStack item) {
        if (item.getType() == XMaterial.PLAYER_HEAD.get()) {
            SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
            skullMeta.setPlayerProfile(player.getPlayerProfile());

            item.setItemMeta(skullMeta);
        }
    }

    public static void applyArenaRecordResetHead(ItemStack item, String recordHolderName) {
        if (item.getType() != XMaterial.PLAYER_HEAD.get()) {
            return;
        }

        if (!Bukkit.getOnlineMode()) {
            applyProfileIfSkull(createOfflineModeResetHeadProfile(), item);
            return;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(recordHolderName);
        if (player != null) {
            applyPlayerProfileIfSkull(player, item);
        }
    }

    public static void applyProfileIfSkull(PlayerProfile profile, ItemStack item) {
        if (item.getType() != XMaterial.PLAYER_HEAD.get()) {
            return;
        }

        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setPlayerProfile(profile);
        item.setItemMeta(skullMeta);
    }

    private static PlayerProfile createOfflineModeResetHeadProfile() {
        PlayerProfile profile = Bukkit.createProfile(OFFLINE_MODE_RESET_HEAD_UUID, OFFLINE_MODE_RESET_HEAD_NAME);
        profile.setProperty(new ProfileProperty("textures", OFFLINE_MODE_RESET_HEAD_TEXTURE_VALUE, OFFLINE_MODE_RESET_HEAD_TEXTURE_SIGNATURE));
        return profile;
    }

    @Contract(pure = true)
    public static ItemStack formatItemStack(SpecialItem specialItem, Var... vars) {
        ItemStack item = specialItem.getItemStack();
        ItemMeta meta = item.getItemMeta();

        String displayName = specialItem.getCustomKey("name");
        Component nameComponent = PLUGIN.getChatManager().parseMessage("<!i>" + displayName, vars);
        meta.displayName(nameComponent);

        List<String> lore = specialItem.getCustomKey("lore");
        if (lore != null) {
            meta.lore(lore.stream().map(line -> PLUGIN.getChatManager().parseMessage("<!i>" + line, vars)).toList());
        }

        boolean decorationOnly = SUPPORTS_1_21_5 && specialItem.getCustomKey("decoration-only") != null;
        if (decorationOnly) {
            meta.setHideTooltip(true);
        }

        item.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
}

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

import dev.despical.commons.miscellaneous.DefaultFontInfo;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
@UtilityClass
public final class StringUtils {

    private static final int CENTER_PX = 165;
    private static final MiniMessage parser = MiniMessage.miniMessage();

    public static void sendCenteredMessage(CommandSender sender, Component component) {
        sender.sendMessage(getCenteredComponent(component));
    }

    public static void sendCenteredMessage(CommandSender sender, String miniMessage) {
        Component component = getCenteredMiniMessage(miniMessage);
        sender.sendMessage(component);
    }

    private static Component getCenteredMiniMessage(String miniMessage) {
        return getCenteredComponent(parser.deserialize(miniMessage));
    }

    private static Component getCenteredComponent(Component component) {
        int messagePxSize = measureComponent(component);
        int spaceWidth = DefaultFontInfo.SPACE.getLength() + 1;
        int paddingSpaces = (CENTER_PX - messagePxSize / 2) / spaceWidth;

        String padding = " ".repeat(Math.max(0, paddingSpaces));
        Component paddingComponent = Component.text(padding);

        return paddingComponent.append(component);
    }

    private static int measureComponent(Component component) {
        int width = 0;

        if (component instanceof TextComponent textComponent) {
            boolean bold = component.style().hasDecoration(TextDecoration.BOLD);
            String content = textComponent.content();

            for (int i = 0; i < content.length(); i++) {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(content.charAt(i));
                width += bold ? dFI.getBoldLength() : dFI.getLength();
                width++;
            }
        }

        for (Component child : component.children()) {
            width += measureComponent(child);
        }

        return width;
    }
}

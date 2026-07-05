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

package dev.despical.tntrun.game.messages;

import net.kyori.adventure.text.Component;

/**
 * @author Despical
 * <p>
 * Created at 28.12.2025
 */
public record TickerAction(
    TickerActionType type,
    Component message,
    Component title,
    Component subtitle,
    String sound
) {

    static TickerAction chat(Component message) {
        return new TickerAction(TickerActionType.CHAT_MESSAGE, message, null, null, null);
    }

    static TickerAction title(Component title, Component subtitle) {
        return new TickerAction(TickerActionType.TITLE, null, title, subtitle, null);
    }

    static TickerAction sound(String sound) {
        return new TickerAction(TickerActionType.PLAY_SOUND, null, null, null, sound);
    }
}

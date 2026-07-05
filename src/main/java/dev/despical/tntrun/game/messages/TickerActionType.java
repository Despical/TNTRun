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

import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.sound.SoundResolver;
import dev.despical.tntrun.user.User;
import org.bukkit.Sound;

/**
 * @author Despical
 * <p>
 * Created at 28.12.2025
 */
public enum TickerActionType {

    CHAT_MESSAGE {

        @Override
        void execute(Game game, TickerAction action) {
            game.broadcastRawComponent(action.message());
        }
    },

    TITLE {

        @Override
        void execute(Game game, TickerAction action) {
            for (User user : game.getUsers()) {
                user.sendTitleComponent(action.title(), action.subtitle(), 0, 20, 0);
            }
        }
    },

    PLAY_SOUND {

        @Override
        void execute(Game game, TickerAction action) {
            String[] split = action.sound().split(",");
            Sound sound = SoundResolver.resolve(split[0]);

            if (sound == null) return;

            float volume = split.length > 1 ? Float.parseFloat(split[1]) : 1F;
            float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1F;

            game.getPlayers().forEach(player -> player.playSound(player.getLocation(), sound, volume, pitch));
        }
    };

    abstract void execute(Game game, TickerAction action);
}

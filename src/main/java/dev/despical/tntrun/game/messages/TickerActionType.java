package dev.despical.tntrun.game.messages;

import dev.despical.commons.XSound;
import dev.despical.tntrun.game.Game;
import dev.despical.tntrun.user.User;

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
            XSound sound = XSound.of(split[0]).orElse(null);

            if (sound == null) return;

            float volume = split.length > 1 ? Float.parseFloat(split[1]) : 1F;
            float pitch = split.length > 2 ? Float.parseFloat(split[2]) : 1F;

            game.getPlayers().forEach(player -> sound.play(player, volume, pitch));
        }
    };

    abstract void execute(Game game, TickerAction action);
}

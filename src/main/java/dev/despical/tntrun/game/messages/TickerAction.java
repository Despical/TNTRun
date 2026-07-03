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

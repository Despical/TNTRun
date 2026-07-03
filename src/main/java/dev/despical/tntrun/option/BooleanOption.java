package dev.despical.tntrun.option;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Despical
 * <p>
 * Created at 12.12.2025
 */
@Getter
@RequiredArgsConstructor
public enum BooleanOption implements ConfigOption<Boolean> {

    DEBUG("debug", false),
    LEVEL_BAR_TIMER("level-bar-timer", false),
    CHAT_DEAD_CHAT_VISIBLE_TO_ALIVE("chat-settings.dead-chat-visible-to-alive", false),
    CHAT_DISABLE_IN_GAME("chat-settings.disable-chat-in-game", false),
    CHAT_ENABLE_FORMATTING("chat-settings.enable-formatting", true),
    CHAT_SEPARATE("chat-settings.separate-chat", true),
    DISABLE_COMMANDS_WHILE_PLAYING("command-settings.disable-commands-while-playing", true),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    INSTANT_LEAVE("instant-leave", false),
    JUMP_BAR("jump-bar", true),
    NAME_TAGS_HIDDEN("name-tags-hidden", false),
    SCOREBOARD_ENABLED("scoreboard-enabled", true),
    UPDATE_NOTIFIER("update-notifier", true);

    private final String path;
    private final Boolean defaultValue;
    private final Class<Boolean> type = Boolean.class;
}

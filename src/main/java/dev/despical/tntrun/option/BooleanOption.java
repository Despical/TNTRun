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

    BLOCK_OUTSIDE_CHAT("chat-settings.block-outside-chat", true),
    DEBUG("debug", false),
    LEVEL_BAR_TIMER("level-bar-timer", false),
    DISABLE_CHAT_IN_GAME("chat-settings.disable-chat-in-game", false),
    DISABLE_COMMANDS_WHILE_PLAYING("command-settings.disable-commands-while-playing", true),
    ENABLE_CHAT_FORMATTING("chat-settings.enable-formatting", true),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    INSTANT_LEAVE("instant-leave", false),
    JUMP_BAR("jump-bar", true),
    LONGEST_SURVIVE_ON_WINS("longest-survive-on-wins", false),
    NAME_TAGS_HIDDEN("name-tags-hidden", false),
    PVP_DISABLED("pvp-disabled", true),
    SCOREBOARD_ENABLED("scoreboard-enabled", true),
    SEPARATE_CHAT("chat-settings.separate-chat", true),
    UPDATE_NOTIFIER("update-notifier", true);

    private final String path;
    private final Boolean defaultValue;
    private final Class<Boolean> type = Boolean.class;
}

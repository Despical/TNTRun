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
    CHAT_FORMAT_ENABLED("Chat-Format-Enabled", true),
    CLEAR_EFFECTS_ON_JOIN("player-settings.clear-effects-on-join", true),
    CLEAR_INVENTORY_ON_JOIN("player-settings.clear-inventory-on-join", true),
    DATABASE_ENABLED("Database-Enabled", false),
    DEBUG("debug", false),
    DISABLE_CHAT_IN_GAME("chat-settings.disable-chat-in-game", false),
    DISABLE_COMMANDS_WHILE_PLAYING("command-settings.disable-commands-while-playing", true),
    DISABLE_FALL_DAMAGE("Disable-Fall-Damage", true),
    DISABLE_SEPARATE_CHAT("Disable-Separate-Chat", false),
    ENABLE_CHAT_FORMATTING("chat-settings.enable-formatting", true),
    EVENT_PROFILING_ENABLED("event-profiling.enabled", false),
    EVENT_PROFILING_VERBOSE("event-profiling.verbose", false),
    GAME_BAR_ENABLED("Game-Bar-Enabled", true),
    INSTANT_LEAVE("Instant-Leave", false),
    JUMP_BAR("Jump-Bar", true),
    LONGEST_SURVIVE_ON_WINS("Longest-Survive-On-Wins", false),
    NAME_TAGS_HIDDEN("Name-Tags-Hidden", false),
    PVP_DISABLED("PVP-Disabled", true),
    SCOREBOARD_ENABLED("scoreboard-enabled", true),
    SEPARATE_CHAT("chat-settings.separate-chat", true),
    UPDATE_NOTIFIER("update-notifier", true),
    UPDATE_NOTIFIER_ENABLED("Update-Notifier-Enabled", true);

    private final String path;
    private final Boolean defaultValue;
    private final Class<Boolean> type = Boolean.class;
}

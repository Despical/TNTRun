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

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

package dev.despical.tntrun.api;

import org.bukkit.event.Event;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Despical
 * <p>
 * Created at 29.01.2026
 */
public final class EventRegistry {

    private EventRegistry() {
    }

    public static Class<? extends Event> getEventClass(EventType type) {
        return type.getEventClass();
    }

    public static boolean matches(EventType type, Event event) {
        return type.getEventClass().isInstance(event);
    }

    public static Set<EventType> getRegisteredTypes() {
        return EnumSet.allOf(EventType.class);
    }
}


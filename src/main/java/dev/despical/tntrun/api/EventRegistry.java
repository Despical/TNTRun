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


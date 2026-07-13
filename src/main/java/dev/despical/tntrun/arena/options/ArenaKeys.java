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

package dev.despical.tntrun.arena.options;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.despical.commons.serializer.LocationSerializer;
import dev.despical.tntrun.game.ArenaPotionEffect;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public final class ArenaKeys {

    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(Location.class, new LocationTypeAdapter())
        .create();

    public static final ArenaOption<Boolean> READY = new ArenaOption<>("ready", false, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<String> MAP_NAME = new ArenaOption<>("map-name", "Unknown Map", String.class) {

        @Override
        protected String parse(String value) {
            return value == null || value.isBlank() ? getDefaultValue() : value;
        }
    };

    public static final ArenaOption<String> MAP_AUTHOR = new ArenaOption<>("map-author", "Unknown Author", String.class) {

        @Override
        protected String parse(String value) {
            return value == null || value.isBlank() ? getDefaultValue() : value;
        }
    };

    public static final ArenaOption<String> RECORD_HOLDER_NAME = new ArenaOption<>("record-holder", "None", String.class) {

        @Override
        protected String parse(String value) {
            return value == null || value.isBlank() ? getDefaultValue() : value;
        }
    };

    public static final ArenaOption<Long> RECORD_TIME = new ArenaOption<>("record-time", -1L, Long.class) {

        @Override
        protected Long parse(String value) {
            return Long.parseLong(value);
        }
    };

    public static final ArenaOption<Location> LOBBY_LOCATION = new ArenaOption<>("lobby", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> START_LOCATION = new ArenaOption<>("start", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Location> END_LOCATION = new ArenaOption<>("end", null, Location.class) {

        @Override
        public Object serialize(Location value) {
            return LocationSerializer.toString(value);
        }

        @Override
        protected Location parse(String value) {
            return LocationSerializer.fromString(value);
        }
    };

    public static final ArenaOption<Integer> MIN_PLAYERS = new ArenaOption<>("min-players", 12, Integer.class) {

        @Override
        protected Integer parse(String value) {
             return Integer.parseInt(value);
        }
    };

    public static final ArenaOption<Integer> MAX_PLAYERS = new ArenaOption<>("max-players", 24, Integer.class) {

        @Override
        protected Integer parse(String value) {
            return Integer.parseInt(value);
        }
    };

    public static final ArenaOption<Boolean> ARENA_SCOREBOARD_ENABLED = new ArenaOption<>("arena-scoreboard-enabled", true, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<Boolean> ARENA_BOSSBAR_ENABLED = new ArenaOption<>("arena-bossbar-enabled", true, Boolean.class) {

        @Override
        protected Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final ArenaOption<List<ArenaPotionEffect>> ARENA_POTION_EFFECTS = new ArenaOption<>("arena-potion-effects", new ArrayList<>(), (Class<List<ArenaPotionEffect>>) (Class<?>) List.class) {

        @Override
        public List<ArenaPotionEffect> deserialize(Object value) {
            if (value instanceof List<?> rawList) {
                List<ArenaPotionEffect> parsedEffects = new ArrayList<>();

                for (Object entry : rawList) {
                    ArenaPotionEffect parsed = parseEffectEntry(entry);
                    if (parsed != null) {
                        parsedEffects.add(parsed);
                    }
                }

                return parsedEffects;
            }

            return super.deserialize(value);
        }

        @Override
        public Object serialize(List<ArenaPotionEffect> value) {
            return value.stream().map(ArenaPotionEffect::toString).toList();
        }

        @Override
        protected List<ArenaPotionEffect> parse(String value) {
            try {
                List<ArenaPotionEffect> parsed = gson.fromJson(value, new TypeToken<List<ArenaPotionEffect>>() {
                }.getType());
                return parsed != null ? parsed : new ArrayList<>();
            } catch (Exception _) {
                ArenaPotionEffect singleEffect = ArenaPotionEffect.fromString(value);

                if (singleEffect == null) {
                    return new ArrayList<>();
                }

                return new ArrayList<>(List.of(singleEffect));
            }
        }

        private ArenaPotionEffect parseEffectEntry(Object entry) {
            if (entry instanceof ArenaPotionEffect potionEffect) {
                return potionEffect;
            }

            if (entry instanceof String stringEntry) {
                return ArenaPotionEffect.fromString(stringEntry);
            }

            if (entry instanceof Map<?, ?> mapEntry) {
                Object effectType = mapEntry.get("effectType");
                Object level = mapEntry.get("level");

                if (effectType instanceof String typeName && level instanceof Number amplifier) {
                    return new ArenaPotionEffect(typeName, amplifier.intValue());
                }
            }

            return null;
        }
    };

    public static List<ArenaOption<?>> getAllKeys() {
        return List.of(
            READY,
            MAP_NAME,
            MAP_AUTHOR,
            RECORD_HOLDER_NAME,
            RECORD_TIME,
            LOBBY_LOCATION,
            START_LOCATION,
            END_LOCATION,
            MIN_PLAYERS,
            MAX_PLAYERS,
            ARENA_SCOREBOARD_ENABLED,
            ARENA_BOSSBAR_ENABLED,
            ARENA_POTION_EFFECTS
        );
    }

    public static List<ArenaOption<?>> getPersistentKeys() {
        return getAllKeys().stream().filter(ArenaOption::isPersistent).toList();
    }
}

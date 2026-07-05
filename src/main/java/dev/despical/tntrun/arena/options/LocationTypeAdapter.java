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

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

/**
 * @author Despical
 * <p>
 * Created at 17.06.2026
 */
public class LocationTypeAdapter implements JsonSerializer<Location>, JsonDeserializer<Location> {

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        if (src == null || src.getWorld() == null) {
            return JsonNull.INSTANCE;
        }

        json.addProperty("world", src.getWorld().getName());
        json.addProperty("x", src.getX());
        json.addProperty("y", src.getY());
        json.addProperty("z", src.getZ());
        json.addProperty("yaw", src.getYaw());
        json.addProperty("pitch", src.getPitch());
        return json;
    }

    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json == null || !json.isJsonObject()) {
            return null;
        }

        JsonObject obj = json.getAsJsonObject();

        World world = Bukkit.getWorld(obj.get("world").getAsString());
        if (world == null) return null;

        double x = obj.get("x").getAsDouble();
        double y = obj.get("y").getAsDouble();
        double z = obj.get("z").getAsDouble();

        float yaw = obj.has("yaw") ? obj.get("yaw").getAsFloat() : 0.0f;
        float pitch = obj.has("pitch") ? obj.get("pitch").getAsFloat() : 0.0f;

        return new Location(world, x, y, z, yaw, pitch);
    }
}

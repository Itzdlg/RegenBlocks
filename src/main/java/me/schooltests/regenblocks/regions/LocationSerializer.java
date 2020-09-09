package me.schooltests.regenblocks.regions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationSerializer implements JsonSerializer<Location>, JsonDeserializer<Location> {
    @Override
    public Location deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonPrimitive p = json.getAsJsonPrimitive();
        String string = p.getAsString();
        String[] parts = string.split(";");

        World w = Bukkit.getWorld(parts[0]);
        double x;
        double y;
        double z;

        try {
            x = Double.parseDouble(parts[1]);
            y = Double.parseDouble(parts[2]);
            z = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid coordinates");
        }

        if (w == null) throw new JsonParseException("Invalid world name");
        return new Location(w, x, y, z);
    }

    @Override
    public JsonElement serialize(Location src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getWorld().getName() + ";" + src.getX() + ";" + src.getY() + ";" + src.getZ());
    }
}

package dc.Persistence.waypoints;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class WaypointDatabase {

    private static final File FILE = new File("plugins/Deathscape/waypoints.json");
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Estructura en memoria temporal para simplificar ejemplo
    private static Map<UUID, Map<String, LocationSerializable>> data = new HashMap<>();

    public static void load() {
        if (!FILE.exists()) {
            save(); // crea archivo vacío
            return;
        }

        try (Reader reader = new FileReader(FILE)) {
            Type type = new TypeToken<Map<String, Map<String, LocationSerializable>>>(){}.getType();
            Map<String, Map<String, LocationSerializable>> raw = gson.fromJson(reader, type);

            if (raw != null) {
                data.clear();
                for (Map.Entry<String, Map<String, LocationSerializable>> entry : raw.entrySet()) {
                    data.put(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try (Writer writer = new FileWriter(FILE)) {
            // Convertir keys UUID a String para JSON
            Map<String, Map<String, LocationSerializable>> raw = new HashMap<>();
            for (Map.Entry<UUID, Map<String, LocationSerializable>> entry : data.entrySet()) {
                raw.put(entry.getKey().toString(), entry.getValue());
            }
            gson.toJson(raw, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addWaypoint(UUID uuid, String name, Location loc) {
        data.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(name, new LocationSerializable(loc));
        save();
    }

    public static void removeWaypoint(UUID uuid, String name) {
        Map<String, LocationSerializable> map = data.get(uuid);
        if (map != null) {
            map.remove(name);
            save();
        }
    }

    public static Map<String, Location> getPlayerWaypoints(UUID uuid) {
        Map<String, LocationSerializable> map = data.get(uuid);
        if (map == null) return new HashMap<>();

        Map<String, Location> result = new HashMap<>();
        for (Map.Entry<String, LocationSerializable> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().toLocation());
        }
        return result;
    }

    public static Set<UUID> getAllPlayersWithWaypoints() {
        return data.keySet();  // Está bien si 'data' es Map<UUID, ...>
    }


    // Clase auxiliar para serializar/deserializar Location
    static class LocationSerializable {
        String world;
        double x, y, z;

        LocationSerializable(Location loc) {
            this.world = loc.getWorld().getName();
            this.x = loc.getX();
            this.y = loc.getY();
            this.z = loc.getZ();
        }

        Location toLocation() {
            World w = Bukkit.getWorld(world);
            return w != null ? new Location(w, x, y, z) : null;
        }
    }
}

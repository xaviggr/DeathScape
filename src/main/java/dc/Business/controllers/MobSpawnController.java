package dc.Business.controllers;

import dc.DeathScape;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntity;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class MobSpawnController {

    private final DeathScape plugin;
    private final MobManager mobManager;
    private final Random random;

    public MobSpawnController(DeathScape plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
        this.random = new Random();
    }

    public String getCustomMobForVanillaMob(String worldName, String vanillaMobType) {
        FileConfiguration config = plugin.getMobsConfig();
        int currentDay = plugin.getServerData().getServerDays();

        String selectedCustomMob = null;

        for (int day = 1; day <= currentDay; day++) {
            List<String> customMobs = config.getStringList("mobs.day_" + day + "." + worldName + "." + vanillaMobType);

            // Si hay mobs configurados para este mob vanilla, reemplaza la selección.
            if (!customMobs.isEmpty()) {
                // Actualiza la selección al mob del día actual
                selectedCustomMob = customMobs.get(random.nextInt(customMobs.size()));
            }
        }

        return selectedCustomMob; // Retorna null si no hay configuración
    }

    public void spawnMythicMob(Location spawnLocation, String mobType) {
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().warning("Spawn location world is null. Aborting MythicMob creation.");
            return;
        }

        // Busca el MythicMob registrado
        Optional<MythicMob> optionalMythicMob = mobManager.getMythicMob(mobType);

        if (optionalMythicMob.isPresent()) {
            MythicMob mythicMob = optionalMythicMob.get();

            // Convierte la ubicación de Bukkit a AbstractLocation
            AbstractLocation abstractLocation = BukkitAdapter.adapt(spawnLocation);

            // Spawnea el mob con sus configuraciones completas
            mythicMob.spawn(abstractLocation, 1, SpawnReason.OTHER);
        } else {
            plugin.getLogger().warning("Could not find MythicMob with type: " + mobType);
        }
    }
}

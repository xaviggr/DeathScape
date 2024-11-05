package dc.Business.controllers;

import dc.DeathScape;
import dc.Business.listeners.MobSpawnListener;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntity;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MobSpawnController {

    private final DeathScape plugin;
    private final MobManager mobManager;
    private final Random random;

    public MobSpawnController(DeathScape plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
        this.random = new Random();
    }

    public List<String> getMobsForTodayInWorld(String worldName) {
        FileConfiguration config = plugin.getMobsConfig();
        int dayOfMonth = LocalDate.now().getDayOfMonth();
        List<String> cumulativeMobs = new ArrayList<>();

        for (int day = 1; day <= dayOfMonth; day++) {
            List<String> mobsForDay = config.getStringList("mobs.day_" + day + "." + worldName);
            cumulativeMobs.addAll(mobsForDay);
        }

        return cumulativeMobs;
    }

    public void spawnMythicMob(Location spawnLocation, String mobType) {
        // Comprobación del mundo
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().warning("El mundo de spawnLocation es null. Abortando la creación del MythicMob.");
            return;
        }

        // Obtener el MythicEntity desde el MobManager
        Optional<MythicEntity> optionalMythicEntity = mobManager.getMythicMob(mobType).map(MythicMob::getMythicEntity);

        if (optionalMythicEntity.isPresent()) {
            MythicEntity mythicEntity = optionalMythicEntity.get();

            // Convierte la Location a AbstractLocation usando BukkitAdapter
            AbstractLocation abstractLocation = BukkitAdapter.adapt(spawnLocation);

            // Llama al método spawn del MythicEntity
            mythicEntity.spawn(abstractLocation, SpawnReason.OTHER);
        } else {
            plugin.getLogger().warning("No se pudo encontrar el MythicMob con el tipo: " + mobType);
        }
    }

    public DeathScape getPlugin() {
        return plugin;
    }
}

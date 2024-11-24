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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MobSpawnController {

    private final DeathScape plugin;
    private final MobManager mobManager;

    /**
     * Constructor for MobSpawnController.
     * Initializes the plugin reference, mob manager, and random generator.
     *
     * @param plugin The main plugin instance.
     */
    public MobSpawnController(DeathScape plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
    }

    /**
     * Retrieves the cumulative list of MythicMobs that are eligible to spawn for the current day
     * in the specified world. Mobs are retrieved from the plugin configuration.
     *
     * @param worldName The name of the world for which to retrieve the mobs.
     * @return A list of mob types available for the current day in the specified world.
     */
    public List<String> getMobsForTodayInWorld(String worldName) {
        FileConfiguration config = plugin.getMobsConfig();
        int dayOfMonth = plugin.getServerData().getServerDays(); // Current day of the month
        List<String> cumulativeMobs = new ArrayList<>();

        // Collect all mobs from day 1 to the current day for the given world
        for (int day = 1; day <= dayOfMonth; day++) {
            List<String> mobsForDay = config.getStringList("mobs.day_" + day + "." + worldName);
            cumulativeMobs.addAll(mobsForDay);
        }

        return cumulativeMobs;
    }

    /**
     * Spawns a MythicMob at the specified location, if the mob type is valid.
     * Logs a warning if the mob type or world is invalid.
     *
     * @param spawnLocation The location where the mob should be spawned.
     * @param mobType       The type of the MythicMob to spawn.
     */
    public void spawnMythicMob(Location spawnLocation, String mobType) {
        // Check if the spawn location's world is valid
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().warning("Spawn location world is null. Aborting MythicMob creation.");
            return;
        }

        // Retrieve the MythicEntity associated with the specified mob type
        Optional<MythicEntity> optionalMythicEntity = mobManager.getMythicMob(mobType).map(MythicMob::getMythicEntity);

        if (optionalMythicEntity.isPresent()) {
            MythicEntity mythicEntity = optionalMythicEntity.get();

            // Convert the Bukkit Location to an AbstractLocation for MythicMobs
            AbstractLocation abstractLocation = BukkitAdapter.adapt(spawnLocation);

            // Spawn the mob at the specified location with a custom spawn reason
            mythicEntity.spawn(abstractLocation, SpawnReason.OTHER);
        } else {
            // Log a warning if the mob type is not found
            plugin.getLogger().warning("Could not find MythicMob with type: " + mobType);
        }
    }

    /**
     * Provides access to the main plugin instance.
     *
     * @return The main plugin instance.
     */
    public DeathScape getPlugin() {
        return plugin;
    }
}

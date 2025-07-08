package dc.Business.controllers;

import dc.DeathScape;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.mobs.MobManager;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.SpawnReason;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Handles the spawning and mapping of custom MythicMobs based on vanilla mob types
 * and server configurations in the DeathScape plugin.
 */
public class MobSpawnController {

    /**
     * Reference to the main DeathScape plugin instance.
     */
    private final DeathScape plugin;

    /**
     * Reference to the MythicMobs MobManager for managing MythicMob entities.
     */
    private final MobManager mobManager;

    /**
     * Random generator used for selecting custom mobs.
     */
    private final Random random;

    /**
     * Constructs a MobSpawnController with a reference to the plugin and initializes necessary managers.
     *
     * @param plugin The main instance of the DeathScape plugin.
     */
    public MobSpawnController(DeathScape plugin) {
        this.plugin = plugin;
        this.mobManager = plugin.getMobManager();
        this.random = new Random();
    }

    /**
     * Gets a custom MythicMob type corresponding to a vanilla mob type based on the current server day and world.
     *
     * <p>The method searches the server configuration for a custom mob type that replaces the specified vanilla mob
     * in the given world. It prioritizes the latest available day configuration.</p>
     *
     * @param worldName     The name of the world where the vanilla mob is located.
     * @param vanillaMobType The type of the vanilla mob to replace.
     * @return The name of the custom MythicMob to spawn, or {@code null} if no configuration is found.
     */
    public String getCustomMobForVanillaMob(String worldName, String vanillaMobType) {
        FileConfiguration config = plugin.getMobsConfig();
        int currentDay = plugin.getServerData().getServerDays();

        String selectedCustomMob = null;

        // Iterate through all days up to the current server day
        for (int day = 1; day <= currentDay; day++) {
            List<String> customMobs = config.getStringList("mobs.day_" + day + "." + worldName + "." + vanillaMobType);

            // If custom mobs are defined for the vanilla mob, update the selection
            if (!customMobs.isEmpty()) {
                selectedCustomMob = customMobs.get(random.nextInt(customMobs.size()));
            }
        }

        return selectedCustomMob; // Returns null if no configuration is found
    }

    /**
     * Spawns a MythicMob at the specified location.
     *
     * <p>This method converts the Bukkit {@link Location} to a MythicMobs-compatible {@link AbstractLocation}
     * and spawns the specified MythicMob at that location. It also logs warnings if the mob type is invalid
     * or if the spawn location's world is null.</p>
     *
     * @param spawnLocation The location where the MythicMob should be spawned.
     * @param mobType       The type of the MythicMob to spawn.
     */
    public void spawnMythicMob(Location spawnLocation, String mobType) {
        if (spawnLocation.getWorld() == null) {
            plugin.getLogger().warning("Spawn location world is null. Aborting MythicMob creation.");
            return;
        }

        // Retrieve the MythicMob by its type
        Optional<MythicMob> optionalMythicMob = mobManager.getMythicMob(mobType);

        if (optionalMythicMob.isPresent()) {
            MythicMob mythicMob = optionalMythicMob.get();

            // Convert the Bukkit location to an AbstractLocation for MythicMobs compatibility
            AbstractLocation abstractLocation = BukkitAdapter.adapt(spawnLocation);

            // Spawn the MythicMob with full configurations
            mythicMob.spawn(abstractLocation, 1, SpawnReason.OTHER);
        } else {
            plugin.getLogger().warning("Could not find MythicMob with type: " + mobType);
        }
    }

    public void spawnRiftMobs(Location spawnLocation) {

    }
}

package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

/**
 * Controls the initialization and configuration of custom dimensions for the DeathScape plugin.
 */
public class DimensionController {

    /**
     * Reference to the main DeathScape plugin instance.
     */
    private final DeathScape plugin;

    /**
     * Constructs a DimensionController with a reference to the main plugin and initializes dimensions.
     *
     * @param deathScape The main instance of the DeathScape plugin.
     */
    public DimensionController(DeathScape deathScape) {
        this.plugin = deathScape;
        init();
    }

    /**
     * Initializes the custom dimensions (spawn and rift) by applying their respective configurations.
     */
    private void init() {
        initRift();
        initSpawn();
    }

    /**
     * Configures the spawn dimension (`world_minecraft_spawn`) with specific game rules
     * to disable environmental changes and damage mechanisms, creating a safe area.
     */
    private void initSpawn() {
        World spawnDimension = Bukkit.getWorld("world_minecraft_spawn");

        if (spawnDimension != null) {
            // Configure game rules for the spawn dimension
            spawnDimension.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false); // Disable day-night cycle
            spawnDimension.setGameRule(GameRule.DO_WEATHER_CYCLE, false); // Disable weather changes
            spawnDimension.setGameRule(GameRule.DO_MOB_SPAWNING, false);  // Disable mob spawning
            spawnDimension.setGameRule(GameRule.DISABLE_RAIDS, true);    // Disable raids
            spawnDimension.setGameRule(GameRule.FALL_DAMAGE, false);    // Disable fall damage
            spawnDimension.setGameRule(GameRule.FIRE_DAMAGE, false);    // Disable fire damage
            spawnDimension.setGameRule(GameRule.NATURAL_REGENERATION, false); // Disable natural food loss
            spawnDimension.setGameRule(GameRule.MOB_GRIEFING, false);   // Disable mob griefing

            // Set the time of day to a constant value (e.g., 6000 ticks for daytime)
            spawnDimension.setTime(6000);

            // Log success message
            Bukkit.getLogger().info("[DeathScape] Spawn dimension initialized successfully.");
        } else {
            // Log an error if the spawn dimension cannot be loaded or created
            Bukkit.getLogger().severe("[DeathScape] Failed to load or create spawn dimension.");
        }
    }

    /**
     * Configures the rift dimension (`world_minecraft_rift`) by keeping the time locked to nighttime.
     */
    private void initRift() {
        World riftDimension = Bukkit.getWorld("world_minecraft_rift");

        // Check if the rift dimension exists
        if (riftDimension != null) {
            // Schedule a repeating task to keep the time locked at nighttime (e.g., 18000 ticks)
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                riftDimension.setTime(18000); // Set the time to night (18000 ticks)
            }, 0L, 100L); // Execute every 5 seconds (100 ticks)
        }
    }
}

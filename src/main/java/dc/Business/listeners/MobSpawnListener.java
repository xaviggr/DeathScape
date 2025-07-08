package dc.Business.listeners;

import dc.Business.controllers.MobSpawnController;
import dc.DeathScape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Listener for handling mob spawns. Replaces natural spawns of vanilla mobs
 * with custom MythicMobs based on the server's configuration.
 */
public class MobSpawnListener implements Listener {

    private final MobSpawnController mobSpawnController;
    private final DeathScape plugin;

    /**
     * Constructs a `MobSpawnListener` with the required plugin and mob spawn controller.
     *
     * @param plugin              The instance of the DeathScape plugin.
     * @param mobSpawnController  The controller managing custom mob spawns.
     */
    public MobSpawnListener(DeathScape plugin, MobSpawnController mobSpawnController) {
        this.mobSpawnController = mobSpawnController;
        this.plugin = plugin;
    }

    /**
     * Handles the `CreatureSpawnEvent` triggered when an entity spawns.
     * Replaces natural spawns of vanilla mobs with custom MythicMobs if configured.
     * Spawns specific mobs in the `world_minecraft_rift` dimension.
     *
     * @param event The `CreatureSpawnEvent` triggered when a creature spawns.
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Only handle naturally spawned mobs
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            return;
        }

        Entity entity = event.getEntity();
        Location spawnLocation = event.getLocation();
        World world = spawnLocation.getWorld();

        if (world == null) {
            return; // Prevent null pointer exceptions
        }

        String worldName = world.getName();

        try {
            if (worldName.equals("world_minecraft_rift")) {
                // Special handling for the rift dimension
                // Cancel the natural spawn
                event.setCancelled(true);

                mobSpawnController.spawnRiftMobs(spawnLocation);
                return;
            }

            // Handle other worlds (natural spawns replaced with configured MythicMobs)
            if (!(entity instanceof Monster)) {
                return; // Only handle monsters
            }

            String vanillaMobType = entity.getType().toString(); // Get the vanilla mob type (e.g., ZOMBIE, SPIDER)

            // Retrieve the custom mob type configured for this vanilla mob
            String customMobType = mobSpawnController.getCustomMobForVanillaMob(worldName, vanillaMobType);

            if (customMobType == null) {
                return; // If no custom mob is configured, do nothing
            }

            // Cancel the natural spawn
            event.setCancelled(true);

            // Spawn the custom MythicMob
            mobSpawnController.spawnMythicMob(spawnLocation, customMobType);

        } catch (Exception e) {
            // Log errors in mob spawning
            plugin.getLogger().severe("Error in mob spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
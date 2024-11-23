package dc.Business.listeners;

import dc.Business.controllers.MobSpawnController;
import dc.DeathScape;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.List;
import java.util.Random;

public class MobSpawnListener implements Listener {

    private final MobSpawnController mobSpawnController;
    private final Random random;

    // Constructor to initialize the MobSpawnController and random number generator
    public MobSpawnListener(DeathScape plugin, MobSpawnController mobSpawnController) {
        this.mobSpawnController = mobSpawnController;
        this.random = new Random();
    }

    /**
     * Handles the creature spawn event. Only processes natural monster spawns.
     * @param event The creature spawn event.
     */
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Only handle NATURAL spawns (not spawns triggered by other means)
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            return;
        }

        Entity entity = event.getEntity();

        // Ensure the entity is a Monster
        if (!(entity instanceof Monster)) {
            return;
        }

        Location spawnLocation = event.getLocation();

        try {
            // Fetch the world name where the mob spawned
            String worldName = entity.getWorld().getName();

            // Get the list of mob types for today in this world
            List<String> todaysMobsForWorld = mobSpawnController.getMobsForTodayInWorld(worldName);

            // If no mobs are configured for today, do nothing
            if (todaysMobsForWorld == null || todaysMobsForWorld.isEmpty()) {
                return;
            }

            // Select a random mob from the list for today's spawn
            String randomMobType = todaysMobsForWorld.get(random.nextInt(todaysMobsForWorld.size()));

            // If the world is null, log a warning and return
            if (spawnLocation.getWorld() == null) {
                mobSpawnController.getPlugin().getLogger().warning("Null world detected, aborting spawn.");
                return;
            }

            // Cancel the default spawn and trigger the custom mob spawn
            event.setCancelled(true);
            mobSpawnController.spawnMythicMob(spawnLocation, randomMobType);

        } catch (Exception e) {
            // Catch any unexpected errors, log them and print stack trace
            mobSpawnController.getPlugin().getLogger().severe("Error in mob spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

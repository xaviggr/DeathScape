package dc.Business.listeners;

import dc.Business.controllers.MobSpawnController;
import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;

public class MobSpawnListener implements Listener {
    private final DeathScape plugin;
    private final MobSpawnController mobSpawnController;
    private final Random random;

    public MobSpawnListener(DeathScape plugin, MobSpawnController mobSpawnController) {
        this.plugin = plugin;
        this.mobSpawnController = mobSpawnController;
        this.random = new Random();
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) return;

        Entity entity = event.getEntity();

        if (!(entity instanceof Monster)) return;
        Location spawnLocation = event.getLocation();

        try {
            if (entity instanceof LivingEntity) {
                String worldName = entity.getWorld().getName();
                List<String> todaysMobsForWorld = mobSpawnController.getMobsForTodayInWorld(worldName);

                if (todaysMobsForWorld == null || todaysMobsForWorld.isEmpty()) {
                    return;
                }

                String randomMobType = todaysMobsForWorld.get(random.nextInt(todaysMobsForWorld.size()));

                if (spawnLocation == null || spawnLocation.getWorld() == null) {
                    mobSpawnController.getPlugin().getLogger().warning("Ubicación nula detectada, abortando spawn.");
                    return;
                }

                event.setCancelled(true);

                mobSpawnController.spawnMythicMob(spawnLocation, randomMobType);
            }
        } catch (Exception e) {
            mobSpawnController.getPlugin().getLogger().severe("Error en spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

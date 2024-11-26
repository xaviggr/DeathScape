package dc.Business.listeners;

import dc.Business.controllers.MobSpawnController;
import dc.DeathScape;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Random;

public class MobSpawnListener implements Listener {

    private final MobSpawnController mobSpawnController;
    private final DeathScape plugin;

    public MobSpawnListener(DeathScape plugin, MobSpawnController mobSpawnController) {
        this.mobSpawnController = mobSpawnController;
        this.plugin = plugin;
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            return;
        }

        Entity entity = event.getEntity();

        if (!(entity instanceof Monster)) {
            return;
        }

        Location spawnLocation = event.getLocation();

        try {
            String worldName = entity.getWorld().getName();
            String vanillaMobType = entity.getType().toString(); // Tipo del mob vanilla (ZOMBIE, SPIDER, etc.)

            // Obtén el mob custom configurado para el mob vanilla
            String customMobType = mobSpawnController.getCustomMobForVanillaMob(worldName, vanillaMobType);

            if (customMobType == null) {
                return; // Si no hay configuración, no hacemos nada
            }

            // Cancelamos el spawn natural
            event.setCancelled(true);

            // Spawneamos el mob custom
            mobSpawnController.spawnMythicMob(spawnLocation, customMobType);

        } catch (Exception e) {
            plugin.getLogger().severe("Error in mob spawn: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

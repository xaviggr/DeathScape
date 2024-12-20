package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;

public class DimensionController {

    private final DeathScape plugin;

    public DimensionController(DeathScape deathScape) {
        this.plugin = deathScape;
        init();
    }

    //Init method to initialize the dimensions
    private void init() {
        initRift();
        initSpawn();
    }

    private void initSpawn() {
        World spawnDimension = Bukkit.getWorld("world_minecraft_spawn");

        if (spawnDimension != null) {
            // Configuración básica del mundo
            spawnDimension.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            spawnDimension.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            spawnDimension.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            // No damage
            spawnDimension.setGameRule(GameRule.DISABLE_RAIDS, true);
            // No fall damage
            spawnDimension.setGameRule(GameRule.FALL_DAMAGE, false);
            // No fire damage
            spawnDimension.setGameRule(GameRule.FIRE_DAMAGE, false);
            spawnDimension.setTime(6000); // Configura el tiempo en el día

            Bukkit.getLogger().info("[DeathScape] Spawn dimension initialized successfully.");
        } else {
            Bukkit.getLogger().severe("[DeathScape] Failed to load or create spawn dimension.");
        }
    }

    private void initRift() {
        World miDimension = Bukkit.getWorld("world_minecraft_rift");

        // Comprueba que la dimensión exista
        if (miDimension != null) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                // Establece el tiempo en noche (por ejemplo, las 18000 ticks)
                miDimension.setTime(18000);
            }, 0L, 100L); // Configura cada 5 segundos (100 ticks) para mantener el tiempo de noche
        }
    }
}

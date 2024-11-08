package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
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
    }

    private void initRift() {
        World miDimension = Bukkit.getWorld("world_minecraft_rift");

        // Comprueba que la dimensión exista
        if (miDimension != null) {
            Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                // Establece el tiempo en noche (por ejemplo, las 18000 ticks)
                miDimension.setTime(18000);
                //Set message on console to test
                Bukkit.getConsoleSender().sendMessage("Time set to night in Rift dimension");
            }, 0L, 100L); // Configura cada 5 segundos (100 ticks) para mantener el tiempo de noche
        } else {
            //Set message on console to test
            Bukkit.getConsoleSender().sendMessage("Rift dimension not found");
        }
    }
}

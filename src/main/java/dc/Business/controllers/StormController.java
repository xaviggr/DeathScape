package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class StormController {

    private DeathScape plugin;
    private ServerController serverController;

    public StormController(DeathScape plugin, ServerController serverController) {
        this.plugin = plugin;
        this.serverController = serverController;
    }

    public void updateStormOnPlayerDeath() {
        serverController.addStormTime(plugin.getMainConfigManager().getStormTime());

        // Inicia la lluvia si no está activa
        if (!Bukkit.getWorlds().get(0).hasStorm()) {
            Bukkit.getWorlds().get(0).setStorm(true);
        }

        // Verifica si ya hay una tarea de lluvia activa para evitar duplicados
        if (!serverController.isRainTaskActive()) {
            serverController.setRainTaskActive(true); // Establece que la tarea está activa

            // Programa una tarea que reducirá el tiempo de lluvia pendiente cada minuto
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (serverController.getStormPendingTime() > 0) {
                        // Reduce el tiempo de lluvia en 1 minuto
                        serverController.reduceStormTime(1);
                    } else {
                        // Si no hay más tiempo de lluvia pendiente, detiene la lluvia y esta tarea
                        Bukkit.getWorlds().get(0).setStorm(false);
                        serverController.setRainTaskActive(false); // Marca la tarea como inactiva
                        this.cancel(); // Cancela esta tarea
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L * 60); // Ejecuta cada minuto (20 ticks = 1 segundo)
        }
    }
}

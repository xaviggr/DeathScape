package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class WeatherController {

    private final DeathScape plugin;
    private final ServerController serverController;

    public WeatherController(DeathScape plugin, ServerController serverController) {
        this.plugin = plugin;
        this.serverController = serverController;
    }

    public void updateStormOnPlayerDeath() {
        serverController.addStormTime(plugin.getMainConfigManager().getStormTime());
        startStormIfNeeded();
    }

    public void checkStormOnServerStart() {
        startStormIfNeeded();
    }

    private void startStormIfNeeded() {
        // Activa la tormenta si hay tiempo pendiente y la tarea no está activa
        if (serverController.getStormPendingTime() > 0 && !serverController.isRainTaskActive()) {
            Bukkit.getWorlds().get(0).setStorm(true);
            serverController.setRainTaskActive(true);
            scheduleStormTimer();
        }
    }

    private void scheduleStormTimer() {
        // Crea una tarea programada para reducir el tiempo de tormenta cada minuto
        new BukkitRunnable() {
            @Override
            public void run() {
                if (serverController.getStormPendingTime() > 0) {
                    serverController.reduceStormTime(1);
                } else {
                    stopStorm();
                    this.cancel(); // Cancela esta tarea
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60); // Ejecuta cada minuto (20 ticks = 1 segundo)
    }

    private void stopStorm() {
        // Detiene la tormenta y marca la tarea como inactiva
        Bukkit.getWorlds().get(0).setStorm(false);
        serverController.setRainTaskActive(false);
    }
}

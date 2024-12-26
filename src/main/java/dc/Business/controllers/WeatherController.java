package dc.Business.controllers;

import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles weather-related functionalities, including storms, time-of-day management,
 * and weather state monitoring for the DeathScape plugin.
 */
public class WeatherController {

    private final DeathScape plugin;
    private final ServerController serverController;

    /**
     * Constructor to initialize the WeatherController with the required plugin and ServerController.
     *
     * @param plugin           The main instance of the DeathScape plugin.
     * @param serverController The controller managing server-related functionalities.
     */
    public WeatherController(DeathScape plugin, ServerController serverController) {
        this.plugin = plugin;
        this.serverController = serverController;
    }

    // --------------------------------------
    // WEATHER MANAGEMENT
    // --------------------------------------

    /**
     * Updates the storm timer when a player dies and starts the storm if needed.
     */
    public void updateStormOnPlayerDeath() {
        serverController.addStormTime(MainConfigManager.getInstance().getStormTime());
        startStormIfNeeded();
    }

    /**
     * Checks if a storm should start on server startup and activates it if necessary.
     */
    public void checkStormOnServerStart() {
        startStormIfNeeded();
    }

    /**
     * Starts a storm if there is pending storm time and the rain task is not already active.
     */
    public void startStormIfNeeded() {
        if (serverController.getStormPendingTime() > 0 && !serverController.isRainTaskActive()) {
            Bukkit.getWorlds().get(0).setStorm(true); // Start the storm in the primary world
            serverController.setRainTaskActive(true);
            scheduleStormTimer();
        }
    }

    /**
     * Stops the storm and marks the rain task as inactive.
     */
    private void stopStorm() {
        Bukkit.getWorlds().get(0).setStorm(false); // Stop the storm in the primary world
        serverController.setRainTaskActive(false);
    }

    // --------------------------------------
    // TIME MANAGEMENT
    // --------------------------------------

    /**
     * Sets the world time to daytime (0 ticks).
     */
    public void setDayTime() {
        Bukkit.getWorlds().get(0).setTime(0); // Set the time to day
    }

    /**
     * Checks if it is currently nighttime in the primary world.
     *
     * @return True if it is nighttime (between 13000 and 23000 ticks), false otherwise.
     */
    public boolean isNightTime() {
        long time = Bukkit.getWorlds().get(0).getTime();
        return time >= 13000 && time <= 23000;
    }

    // --------------------------------------
    // WEATHER STATE MONITORING
    // --------------------------------------

    /**
     * Checks if it is currently thundering in the primary world.
     *
     * @return True if it is thundering, false otherwise.
     */
    public boolean isThundering() {
        return Bukkit.getWorlds().get(0).isThundering();
    }

    // --------------------------------------
    // TASK SCHEDULING
    // --------------------------------------

    /**
     * Schedules a repeating task to reduce the storm timer every minute.
     * If the storm timer reaches 0, the storm is stopped, and the task is canceled.
     */
    private void scheduleStormTimer() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (serverController.getStormPendingTime() > 0) {
                    serverController.reduceStormTime(1); // Reduce storm time by 1 minute
                } else {
                    stopStorm(); // Stop the storm when time reaches 0
                    this.cancel(); // Cancel the scheduled task
                }
            }
        }.runTaskTimer(plugin, 0L, 20L * 60); // Run every minute (20 ticks = 1 second)
    }
}

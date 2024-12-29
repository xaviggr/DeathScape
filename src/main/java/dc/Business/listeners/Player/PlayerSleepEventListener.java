package dc.Business.listeners.Player;

import dc.Business.controllers.WeatherController;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

/**
 * Listener for handling player sleep events. Manages nighttime sleep,
 * weather effects, and transitions to daytime.
 */
public class PlayerSleepEventListener implements Listener {

    private final WeatherController weatherController;

    /**
     * Constructs a `PlayerSleepEventListener` with the required weather controller.
     *
     * @param weatherController The controller managing weather-related functionality.
     */
    public PlayerSleepEventListener(WeatherController weatherController) {
        this.weatherController = weatherController;
    }

    /**
     * Handles the `PlayerBedEnterEvent` triggered when a player attempts to sleep.
     * Manages transitions to daytime and ensures storm weather is maintained if active.
     *
     * @param event The `PlayerBedEnterEvent` triggered when a player enters a bed.
     */
    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Cancel the event if it's not nighttime
        if (!weatherController.isNightTime()) {
            event.setCancelled(true);
            Message.sendMessage(player, "No puedes dormir durante el día.", ChatColor.RED);
            return;
        }

        // Cancel the event if it's storming during the daytime
        if (world.hasStorm() && !weatherController.isNightTime()) {
            event.setCancelled(true);
            Message.sendMessage(player, "No puedes dormir durante la tormenta.", ChatColor.RED);
            return;
        }

        // If this is the first player sleeping, transition to daytime
        if (weatherController.isNightTime()) { // Double-check if it's still nighttime
            world.setTime(0); // Set the time to daytime
            Message.sendMessageAllPlayers("El jugador " + player.getName() + " ha hecho que amanezca.", ChatColor.GOLD);
        }

        // Ensure storm weather remains active if it was active
        if (world.hasStorm()) {
            world.setStorm(true);
        }
    }

    /**
     * Handles the `PlayerBedLeaveEvent` triggered when a player leaves a bed.
     * Removes the player from the list of sleeping players.
     *
     * @param event The `PlayerBedLeaveEvent` triggered when a player leaves a bed.
     */
    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {

    }
}
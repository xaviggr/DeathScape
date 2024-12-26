package dc.Business.listeners.Player;

import dc.Business.controllers.AnimationController;
import dc.Business.controllers.PlayerController;
import dc.Business.controllers.TotemController;
import dc.Business.controllers.WeatherController;
import dc.DeathScape;

/**
 * Registers all player-related event listeners in the plugin.
 * Centralizes the initialization and management of player listeners.
 */
public class PlayerListener {

    /**
     * Constructs a `PlayerListener` and registers all event listeners for player-related functionality.
     *
     * @param plugin              The instance of the DeathScape plugin.
     * @param playerController    The controller managing player-related functionality.
     * @param weatherController   The controller managing weather-related functionality.
     * @param animationController The controller managing animations such as death animations.
     * @param totemController     The controller managing Totem of Undying functionality.
     */
    public PlayerListener(DeathScape plugin, PlayerController playerController, WeatherController weatherController, AnimationController animationController, TotemController totemController) {

        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(playerController, weatherController, animationController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(plugin, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(plugin, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerSleepEventListener(weatherController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityResurrectEventListener(totemController, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerKillListener(playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(playerController, plugin), plugin);
    }
}
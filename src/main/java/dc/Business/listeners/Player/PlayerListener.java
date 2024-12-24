package dc.Business.listeners.Player;

import dc.Business.controllers.AnimationController;
import dc.Business.controllers.PlayerController;
import dc.Business.controllers.TotemController;
import dc.Business.controllers.WeatherController;
import dc.DeathScape;

public class PlayerListener {

    public PlayerListener(DeathScape plugin, PlayerController playerController, WeatherController weatherController, AnimationController animationController, TotemController totemController) {

        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(playerController, weatherController, animationController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(plugin,playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerDamageListener(plugin), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(plugin, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerSleepEventListener(weatherController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityResurrectEventListener(totemController, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerKillListener(playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerRespawnListener(playerController), plugin);
    }
}

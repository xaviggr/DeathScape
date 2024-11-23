package dc.Business.listeners.Player;

import dc.Business.controllers.AnimationController;
import dc.Business.controllers.PlayerController;
import dc.Business.controllers.TotemController;
import dc.Business.controllers.WeatherController;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener {

    public PlayerListener(JavaPlugin plugin, PlayerController playerController, WeatherController weatherController, AnimationController animationController, TotemController totemController) {

        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(new PlayerDeathEventListener(playerController, weatherController, animationController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerJoinEventListener(playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerQuitEventListener(playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerSleepEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerWakeEventListener(), plugin);
        plugin.getServer().getPluginManager().registerEvents(new EntityResurrectEventListener(totemController, playerController), plugin);
        plugin.getServer().getPluginManager().registerEvents(new PlayerKillListener(playerController), plugin);
    }
}

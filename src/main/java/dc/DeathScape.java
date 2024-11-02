package dc;

import dc.Business.controllers.*;
import dc.Persistence.config.MainConfigManager;
import dc.Business.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

public class DeathScape extends JavaPlugin {

    private MainConfigManager mainConfigManager;
    private ServerController serverController;

    //Listeners
    PlayerListener playerListener;

    //Controllers
    PlayerController playerController;
    StormController stormController;
    TotemController totemController;
    AnimationController animationController;

    public HashMap<String, Long> time_of_connection;

    public ServerController getServerData() {
        return serverController;
    }

    @Override
    public void onEnable() {
        serverController = new ServerController(this);
        mainConfigManager = new MainConfigManager(this);
        playerController = new PlayerController(this);
        stormController = new StormController(this, serverController);
        totemController = new TotemController(this);
        animationController = new AnimationController(this);


        playerListener = new PlayerListener(this, serverController, playerController, stormController, totemController, animationController);

        time_of_connection = new HashMap<>();

        new onUpdate().runTaskTimer(this, 0, 1);
        registerCommands();
        registerEvents();
        stormController.checkStormOnServerStart();
        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(playerListener, this);
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(new DeathScapeCommand(this));
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }

    private static class onUpdate extends BukkitRunnable {
        @Override
        public void run() {
            // Este método se ejecutará en cada tick del juego
        }
    }
}

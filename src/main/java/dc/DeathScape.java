package dc;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.StormController;
import dc.Business.controllers.TotemController;
import dc.Business.inventory.ReviveInventory;
import dc.Persistence.config.MainConfigManager;
import dc.Business.controllers.ServerController;
import dc.Business.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

public class DeathScape extends JavaPlugin {

    private MainConfigManager mainConfigManager;
    private DeathScapeCommand deathScapeCommand;

    //Listeners
    PlayerListener playerListener;
    ReviveInventory reviveInventory;

    //Controllers
    PlayerController playerController;
    StormController stormController;
    TotemController totemController;
    private ServerController serverController;

    public HashMap<String, Long> time_of_connection;

    // Getters
    public ServerController getServerData() {
        return serverController;
    }

    @Override
    public void onEnable() {
        //Config
        mainConfigManager = new MainConfigManager(this);

        //Controllers
        serverController = new ServerController(this);
        playerController = new PlayerController(this);
        stormController = new StormController(this, serverController);
        totemController = new TotemController(this);

        //Listeners
        playerListener = new PlayerListener(this, serverController, playerController, stormController, totemController);
        reviveInventory = new ReviveInventory();

        deathScapeCommand = new DeathScapeCommand(this, reviveInventory);

        //HashMaps
        time_of_connection = new HashMap<>();

        new onUpdate().runTaskTimer(this, 0, 1);

        // Register commands and events
        registerCommands();
        registerEvents();

        // Check if the storm is active when the server starts
        stormController.checkStormOnServerStart();

        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(reviveInventory, this);
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(deathScapeCommand);
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

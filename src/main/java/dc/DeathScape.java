package dc;

import dc.Business.controllers.*;
import dc.Business.inventory.ReportInventory;
import dc.Business.inventory.ReportsInventory;
import dc.Business.listeners.ChatListener;
import dc.Business.listeners.MobSpawnListener;
import dc.Business.inventory.ReviveInventory;
import dc.Persistence.config.MainConfigManager;
import dc.Business.listeners.PlayerListener;
import io.lumine.mythic.api.MythicProvider;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import io.lumine.mythic.api.mobs.MobManager; // Asegúrate de importar MobManager

public class DeathScape extends JavaPlugin {
    private FileConfiguration mobsConfig;
    private ServerController serverController;
    private MobManager mobManager; // Añade esta línea
    private DeathScapeCommand deathScapeCommand;

    // Listeners
    PlayerListener playerListener;
    MobSpawnListener mobSpawnListener;
    ChatListener chatListener;

    // Inventory
    ReportInventory reportInventory;
    ReviveInventory reviveInventory;
    ReportsInventory reportsInventory;

    // Controllers
    PlayerController playerController;
    WeatherController weatherController;
    MobSpawnController mobSpawnController;
    TotemController totemController;
    DimensionController dimensionController;

    public HashMap<String, Long> time_of_connection;

    public ServerController getServerData() {
        return serverController;
    }
    public MobManager getMobManager() {
        return mobManager; // Método para obtener el MobManager
    }

    @Override
    public void onEnable() {
        //Config
        loadMobsConfig();

        //Singletons
        MainConfigManager.setInstance(this);

        mobManager = MythicProvider.get().getMobManager();
        serverController = new ServerController(this);

        //Controllers
        serverController = new ServerController(this);
        playerController = new PlayerController(this);
        weatherController = new WeatherController(this, serverController);
        totemController = new TotemController(this);
        mobSpawnController = new MobSpawnController(this);
        dimensionController = new DimensionController(this);

        //Inventory
        reportInventory = new ReportInventory();
        reviveInventory = new ReviveInventory();
        reportsInventory = new ReportsInventory();

        //Listeners
        AnimationController animationController = new AnimationController(this);
        playerListener = new PlayerListener(this, serverController, playerController, weatherController, totemController, animationController);
        mobSpawnListener = new MobSpawnListener(this, mobSpawnController);
        deathScapeCommand = new DeathScapeCommand(this, reportInventory, reportsInventory);
        chatListener = new ChatListener();

        //HashMaps
        time_of_connection = new HashMap<>();

        new onUpdate().runTaskTimer(this, 0, 1);

        // Register commands and events
        registerCommands();
        registerEvents();

        // Check if the storm is active when the server starts
        weatherController.checkStormOnServerStart();

        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    // Método para cargar mobs.yml
    public void loadMobsConfig() {
        File mobsFile = new File(getDataFolder(), "mobs.yml");
        if (!mobsFile.exists()) {
            saveResource("mobs.yml", false); // Copia mobs.yml desde src/main/resources si no existe
        }
        mobsConfig = YamlConfiguration.loadConfiguration(mobsFile);
    }

    public FileConfiguration getMobsConfig() {
        return mobsConfig;
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(playerListener, this);
        getServer().getPluginManager().registerEvents(reportInventory, this);
        getServer().getPluginManager().registerEvents(reviveInventory, this);
        getServer().getPluginManager().registerEvents(reportsInventory, this);
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(deathScapeCommand);
    }

    private static class onUpdate extends BukkitRunnable {
        @Override
        public void run() {
            // Este método se ejecutará en cada tick del juego
        }
    }
}


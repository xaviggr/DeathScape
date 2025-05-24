package dc;

import dc.Business.controllers.*;
import dc.Business.inventory.ReportInventory;
import dc.Business.inventory.ReportsInventory;
import dc.Business.inventory.ReviveInventory;
import dc.Business.listeners.ChatListener;
import dc.Business.listeners.MobSpawnListener;
import dc.Business.listeners.Player.PlayerListener;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

/**
 * Main class for the DeathScape plugin.
 * Handles initialization, configuration, controllers, and listeners.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DeathScape extends JavaPlugin {

    // Configuration files
    private FileConfiguration mobsConfig;
    private FileConfiguration difficultiesConfig;

    // Controllers
    private ServerController serverController;
    private PlayerController playerController;
    private WeatherController weatherController;
    private MobSpawnController mobSpawnController;
    private TotemController totemController;
    private DimensionController dimensionController;
    private DungeonController dungeonController;
    private ItemsController itemsController;

    // Listeners
    private PlayerListener playerListener;
    private MobSpawnListener mobSpawnListener;
    private ChatListener chatListener;

    // Inventories
    private ReportInventory reportInventory;
    private ReviveInventory reviveInventory;
    private ReportsInventory reportsInventory;

    // Other
    public HashMap<String, Long> time_of_connection; // Tracks player connection times
    private MobManager mobManager;
    private DeathScapeCommand deathScapeCommand;
    private LifeController lifeController;

    /**
     * Called when the plugin is enabled.
     * Initializes configuration, controllers, inventories, and listeners.
     */
    @Override
    public void onEnable() {
        // Initialize configuration and singleton instances
        MainConfigManager.setInstance(this);
        loadMobsConfig();
        loadDifficultiesConfig();
        PlayerDatabase.initPlayerDatabase();
        GroupDatabase.initGroupDatabase();

        // Check for MythicMobs plugin
        if (getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            mobManager = MythicProvider.get().getMobManager();
        } else {
            mobManager = null;
            getLogger().warning("MythicMobs not found. Some features will be limited.");
        }

        // Initialize controllers, inventories, and listeners
        initializeControllers();
        initializeInventories();
        initializeListeners();

        // Initialize connection tracking
        time_of_connection = new HashMap<>();

        // Register commands and events
        registerCommands();
        registerEvents();

        // Check storm status on server start
        weatherController.checkStormOnServerStart();

        // Console message
        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    /**
     * Initializes the plugin controllers.
     */
    private void initializeControllers() {
        serverController = new ServerController(this);
        playerController = new PlayerController(this);
        weatherController = new WeatherController(this, serverController);
        totemController = new TotemController(this);
        mobSpawnController = new MobSpawnController(this);
        dimensionController = new DimensionController(this);
        dungeonController = new DungeonController(this, playerController);
        itemsController = new ItemsController(this);
        lifeController = new LifeController(this);
    }

    /**
     * Initializes the plugin inventories.
     */
    private void initializeInventories() {
        reportInventory = new ReportInventory();
        reviveInventory = new ReviveInventory();
        reportsInventory = new ReportsInventory();
    }

    /**
     * Initializes the plugin listeners.
     */
    private void initializeListeners() {
        AnimationController animationController = new AnimationController(this);
        playerListener = new PlayerListener(this, playerController, weatherController, animationController, totemController, lifeController);
        mobSpawnListener = new MobSpawnListener(this, mobSpawnController);
        deathScapeCommand = new DeathScapeCommand(this, reportInventory, reportsInventory, playerController, reviveInventory, dungeonController, lifeController);
        chatListener = new ChatListener(playerController, this);
    }

    /**
     * Loads the mobs configuration file (mobs.yml).
     */
    public void loadMobsConfig() {
        File mobsFile = new File(getDataFolder(), "mobs.yml");
        if (!mobsFile.exists()) {
            saveResource("mobs.yml", false);
        }
        mobsConfig = YamlConfiguration.loadConfiguration(mobsFile);
    }

    /**
     * Loads the difficulties configuration file (dificultades_info.yml).
     */
    public void loadDifficultiesConfig() {
        File difficultiesFile = new File(getDataFolder(), "dificultades_info.yml");
        if (!difficultiesFile.exists()) {
            saveResource("dificultades_info.yml", false);
        }
        difficultiesConfig = YamlConfiguration.loadConfiguration(difficultiesFile);
    }

    /**
     * Gets the mobs configuration.
     * @return The mobs configuration file.
     */
    public FileConfiguration getMobsConfig() {
        return mobsConfig;
    }

    /**
     * Gets the difficulties configuration.
     * @return The difficulties configuration file.
     */
    public FileConfiguration getDifficultiesConfig() {
        return difficultiesConfig;
    }

    /**
     * Called when the plugin is disabled.
     */
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
        playerController.removePlayersFromServer();
    }

    /**
     * Registers plugin events with the Bukkit plugin manager.
     */
    private void registerEvents() {
        getServer().getPluginManager().registerEvents(reportInventory, this);
        getServer().getPluginManager().registerEvents(reviveInventory, this);
        getServer().getPluginManager().registerEvents(reportsInventory, this);
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    /**
     * Registers plugin commands.
     */
    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(deathScapeCommand);
    }

    /**
     * Gets the server controller.
     * @return The server controller instance.
     */
    public ServerController getServerData() {
        return serverController;
    }

    /**
     * Gets the MythicMobs mob manager.
     * @return The mob manager, or null if MythicMobs is not enabled.
     */
    public MobManager getMobManager() {
        return mobManager;
    }
}
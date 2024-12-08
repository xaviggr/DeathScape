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
import dc.Business.controllers.AnimationController;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MobManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class DeathScape extends JavaPlugin {
    // Configuración
    private FileConfiguration mobsConfig;

    // Controladores
    private ServerController serverController;
    private PlayerController playerController;
    private WeatherController weatherController;
    private MobSpawnController mobSpawnController;
    private TotemController totemController;
    private DimensionController dimensionController;

    // Listeners
    private PlayerListener playerListener;
    private MobSpawnListener mobSpawnListener;
    private ChatListener chatListener;

    // Inventarios
    private ReportInventory reportInventory;
    private ReviveInventory reviveInventory;
    private ReportsInventory reportsInventory;

    // Otros
    public HashMap<String, Long> time_of_connection;
    private MobManager mobManager;
    private DeathScapeCommand deathScapeCommand;

    @Override
    public void onEnable() {
        // Inicialización de configuración y singleton
        MainConfigManager.setInstance(this);
        loadMobsConfig();
        PlayerDatabase.initPlayerDatabase();
        GroupDatabase.initGroupDatabase();

        // Verificar MythicMobs
        if (getServer().getPluginManager().isPluginEnabled("MythicMobs")) {
            mobManager = MythicProvider.get().getMobManager();
        } else {
            mobManager = null;
            getLogger().warning("MythicMobs no encontrado. Algunas funciones estarán limitadas.");
        }

        // Inicialización de controladores
        initializeControllers();

        // Inicialización de inventarios
        initializeInventories();

        // Inicialización de listeners
        initializeListeners();

        // HashMap de conexiones
        time_of_connection = new HashMap<>();

        // Registro de comandos y eventos
        registerCommands();
        registerEvents();

        // Verificar tormenta al iniciar el servidor
        weatherController.checkStormOnServerStart();

        // Mensaje de consola
        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    private void initializeControllers() {
        serverController = new ServerController(this);
        playerController = new PlayerController(this);
        weatherController = new WeatherController(this, serverController);
        totemController = new TotemController(this);
        mobSpawnController = new MobSpawnController(this);
        dimensionController = new DimensionController(this);
    }

    private void initializeInventories() {
        reportInventory = new ReportInventory();
        reviveInventory = new ReviveInventory();
        reportsInventory = new ReportsInventory();
    }

    private void initializeListeners() {
        AnimationController animationController = new AnimationController(this);
        playerListener = new PlayerListener(this, playerController, weatherController, animationController, totemController);
        mobSpawnListener = new MobSpawnListener(this, mobSpawnController);
        deathScapeCommand = new DeathScapeCommand(this, reportInventory, reportsInventory, playerController, reviveInventory);
        chatListener = new ChatListener(playerController);
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

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(reportInventory, this);
        getServer().getPluginManager().registerEvents(reviveInventory, this);
        getServer().getPluginManager().registerEvents(reportsInventory, this);
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
        getServer().getPluginManager().registerEvents(chatListener, this);
    }

    private void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(deathScapeCommand);
    }

    // Métodos públicos para acceder a datos
    public ServerController getServerData() {
        return serverController;
    }

    public MobManager getMobManager() {
        return mobManager;
    }
}

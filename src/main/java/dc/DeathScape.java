package dc;

import dc.Business.controllers.*;
import dc.Business.listeners.MobSpawnListener;
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
    private MainConfigManager mainConfigManager;
    private ServerController serverController;
    private MobManager mobManager; // Añade esta línea

    // Listeners
    PlayerListener playerListener;
    MobSpawnListener mobSpawnListener;

    // Controllers
    PlayerController playerController;
    StormController stormController;
    MobSpawnController mobSpawnController;
    TotemController totemController;

    public HashMap<String, Long> time_of_connection;

    public ServerController getServerData() {
        return serverController;
    }
    public MobManager getMobManager() {
        return mobManager; // Método para obtener el MobManager
    }

    @Override
    public void onEnable() {
        // Cargar configuración principal y otras inicializaciones
        loadMobsConfig(); // Llamada para cargar mobs.yml

        mobManager = MythicProvider.get().getMobManager();

        serverController = new ServerController(this);
        mainConfigManager = new MainConfigManager(this);
        playerController = new PlayerController(this);
        stormController = new StormController(this, serverController);
        totemController = new TotemController(this);
        mobSpawnController = new MobSpawnController(this); // Asegúrate de pasar la referencia correcta

        playerListener = new PlayerListener(this, serverController, playerController, stormController, totemController);
        mobSpawnListener = new MobSpawnListener(this, mobSpawnController);

        time_of_connection = new HashMap<>();

        new onUpdate().runTaskTimer(this, 0, 1);
        registerCommands();
        registerEvents();
        stormController.checkStormOnServerStart();
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
        getServer().getPluginManager().registerEvents(mobSpawnListener, this);
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


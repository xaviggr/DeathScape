package dc;

import dc.config.MainConfigManager;
import dc.listeners.PlayerListener;
import dc.server.ServerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Objects;

public class DeathScape extends JavaPlugin {

    private MainConfigManager mainConfigManager;
    public HashMap<String, Long> tiempoDeConexion;
    private ServerData serverData;  // Define serverData aquí

    public ServerData getServerData() {
        return serverData;
    }

    @Override
    public void onEnable() {
        serverData = new ServerData(this);  // Inicializa serverData
        mainConfigManager = new MainConfigManager(this);
        tiempoDeConexion = new HashMap<>();

        new onUpdate().runTaskTimer(this, 0, 1);  // Inicia la tarea programada
        registerCommands();
        registerEvents();  // Registra eventos

        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(new DeathScapeCommand(this));
    }

    public void registerEvents() {
        // Pasa tanto `this` como `serverData` al constructor de `PlayerListener`
        getServer().getPluginManager().registerEvents(new PlayerListener(this, serverData), this);
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

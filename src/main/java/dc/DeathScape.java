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

    public ServerData getServerData() {
        return serverData;
    }

    private ServerData serverData;

    @Override
    public void onEnable() {
        serverData = new ServerData(this);
        new onUpdate().runTaskTimer(this, 0, 1);
        mainConfigManager = new MainConfigManager(this);
        tiempoDeConexion = new HashMap<>();
        registerCommands();
        registerEvents();
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
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
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

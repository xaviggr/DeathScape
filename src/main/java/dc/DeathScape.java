package dc;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class DeathScape extends JavaPlugin {
    @Override
    public void onEnable() {
        registerCommands();
        Bukkit.getConsoleSender().sendMessage("DeathScape has been enabled!");
    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("DeathScape has been disabled!");
    }

    public void registerCommands() {
        Objects.requireNonNull(this.getCommand("deathscape")).setExecutor(new DeathScapeCommand());
    }
}

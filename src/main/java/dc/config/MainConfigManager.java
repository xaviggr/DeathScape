package dc.config;

import dc.DeathScape;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainConfigManager {
    private DeathScape plugin;

    private Boolean isWelcomeMessageEnabled;
    private String welcomeMessage;
    private String deathMessageTitle;
    private String deathMessageSubtitle;
    private String banMessage;
    private boolean isBanMessageEnabled;

    public boolean isKick_if_ip_changed() {
        return kick_if_ip_changed;
    }

    private boolean kick_if_ip_changed;

    public String getBanMessage() {
        return banMessage;
    }

    public boolean isBanMessageEnabled() {
        return isBanMessageEnabled;
    }

    public String getDeathMessageTitle() {
        return deathMessageTitle;
    }

    public String getDeathMessageSubtitle() {
        return deathMessageSubtitle;
    }

    public Boolean getWelcomeMessageEnabled() {
        return isWelcomeMessageEnabled;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public MainConfigManager(DeathScape plugin) {
        this.plugin = plugin;
        PlayerDatabase.setNombreArchivo(plugin.getDataFolder () + File.separator + "players.json");
        loadConfig();
    }

    public void loadConfig() {
        //Messages
        isWelcomeMessageEnabled = getConfig().getBoolean("messages.welcome_message.enabled");
        welcomeMessage = getConfig().getString("messages.welcome_message.message");

        deathMessageTitle = getConfig().getString("messages.death_message.title");
        deathMessageSubtitle = getConfig().getString("messages.death_message.subtitle");

        isBanMessageEnabled = getConfig().getBoolean("messages.ban_message.enabled");
        banMessage = getConfig().getString("messages.ban_message.message");

        //Config
        kick_if_ip_changed = getConfig ().getBoolean ("config.kick_if_ip_changed");

    }

    public boolean reloadConfig() {
        try {
            if (fileConfiguration == null) {
                File configFile = new File (plugin.getDataFolder (), "config.yml");
                if (!configFile.exists ()) {
                    plugin.saveResource ("config.yml", false);
                }
                fileConfiguration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration (configFile);
            }
            loadConfig ();

        } catch (Exception e) {
            e.printStackTrace ();
            return false;
        }

        PlayerDatabase.initPlayerDatabase ();

        return true;
    }

    private FileConfiguration fileConfiguration = null;
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }
}

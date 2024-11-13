package dc.Persistence.config;

import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainConfigManager {
    private static MainConfigManager instance;
    private DeathScape plugin;

    private Boolean isWelcomeMessageEnabled;
    private String welcomeMessage;
    private String deathMessageTitle;
    private String deathMessageSubtitle;
    private String banMessage;
    private int stormTime;
    private boolean isBanMessageEnabled;
    private int points_to_reduce_on_death;

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

    public int getStormTime() { return stormTime; }

    public int getPoints_to_reduce_on_death() {
        return points_to_reduce_on_death;
    }

    // Constructor privado, sólo puede ser llamado desde el Singleton
    private MainConfigManager() {
        // Este constructor es ahora privado, no se inicializa con el plugin aquí
    }

    // Método estático para acceder al Singleton, recibe el plugin solo la primera vez
    public static void setInstance(DeathScape plugin) {
        if (instance == null) {
            instance = new MainConfigManager();
            instance.plugin = plugin; // Aquí se asigna el plugin solo la primera vez
            PlayerDatabase.setNameFile(plugin.getDataFolder() + File.separator + "players.json");
            BannedWordsDatabase.setBannedWordsFile(plugin.getDataFolder() + File.separator + "bannedWords.json");
            ReportsDatabase.setReportsFile(plugin.getDataFolder() + File.separator + "reports.json");
            GroupDatabase.setNameFile(plugin.getDataFolder() + File.separator + "groups.json");
            instance.loadConfig();
        }
    }

    // Este método ahora no necesita parámetros, porque ya se ha asignado el plugin en el primer uso
    public static MainConfigManager getInstance() {
        return instance;
    }

    public void loadConfig() {
        // Cargar configuraciones
        isWelcomeMessageEnabled = (Boolean) getConfig().getBoolean("messages.welcome_message.enabled");
        welcomeMessage = getConfig().getString("messages.welcome_message.message");
        deathMessageTitle = getConfig().getString("messages.death_message.title");
        deathMessageSubtitle = getConfig().getString("messages.death_message.subtitle");
        isBanMessageEnabled = getConfig().getBoolean("messages.ban_message.enabled");
        banMessage = getConfig().getString("messages.ban_message.message");
        kick_if_ip_changed = getConfig().getBoolean("config.kick_if_ip_changed");
        stormTime = getConfig().getInt("config.storm_time");
        points_to_reduce_on_death = getConfig().getInt("config.points_to_reduce_on_death");
    }

    public List<GroupData> getGroupsFromConfig() {
        List<GroupData> groups = new ArrayList<>();
        ConfigurationSection groupsSection = getConfig().getConfigurationSection("groups");
        if (groupsSection == null) return null;

        for (String groupName : groupsSection.getKeys(false)) {
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
            if (groupSection == null) continue;

            String prefix = groupSection.getString("prefix", "");
            List<Permission> permissions = new ArrayList<>();
            for (String perm : groupSection.getStringList("permissions")) {
                Permission permission = Permission.valueOf(perm.toUpperCase());
                permissions.add(permission);
            }

            List<PlayerData> players = new ArrayList<>();
            for (String playerName : groupSection.getStringList("players")) {
                PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(playerName);
                if (playerData != null) {
                    players.add(playerData);
                }
            }

            GroupData groupData = new GroupData(groupName, prefix, permissions, players);
            groups.add(groupData);
        }
        return groups;
    }

    private FileConfiguration fileConfiguration = null;

    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        try {
            if (fileConfiguration == null) {
                File configFile = new File(plugin.getDataFolder(), "config.yml");
                if (!configFile.exists()) {
                    plugin.saveResource("config.yml", false);
                }
                fileConfiguration = org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(configFile);
            }
            loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
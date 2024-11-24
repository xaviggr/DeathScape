package dc.Persistence.config;

import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
import dc.Business.log.LogData;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.logs.LogDatabase;
import dc.Persistence.player.PlayerDatabase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainConfigManager {
    private static MainConfigManager instance;
    private DeathScape plugin;

    // Configuración de mensajes
    private Boolean isWelcomeMessageEnabled;
    private String welcomeMessage;
    private String deathMessageTitle;
    private String deathMessageSubtitle;
    private String banMessage;
    private boolean isBanMessageEnabled;

    // Configuración general
    private boolean kickIfIpChanged;
    private int stormTime;
    private int pointsToReduceOnDeath;

    // Constructor privado
    private MainConfigManager() {}

    // Método estático para acceder al Singleton
    public static void setInstance(DeathScape plugin) {
        if (instance == null) {
            instance = new MainConfigManager();
            instance.plugin = plugin;
            PlayerDatabase.setNameFile(plugin.getDataFolder() + File.separator + "players.json");
            BannedWordsDatabase.setBannedWordsFile(plugin.getDataFolder() + File.separator + "bannedWords.json");
            ReportsDatabase.setReportsFile(plugin.getDataFolder() + File.separator + "reports.json");
            GroupDatabase.setNameFile(plugin.getDataFolder() + File.separator + "groups.json");
            LogDatabase.setLogFile(plugin.getDataFolder() + File.separator + "logs.json");
            instance.loadConfig();
        }
    }

    // Obtener la instancia del Singleton
    public static MainConfigManager getInstance() {
        return instance;
    }

    // Cargar la configuración
    public void loadConfig() {
        loadMessageConfig();
        loadGeneralConfig();
    }

    private void loadMessageConfig() {
        isWelcomeMessageEnabled = getConfig().getBoolean("messages.welcome_message.enabled");
        welcomeMessage = getConfig().getString("messages.welcome_message.message");
        deathMessageTitle = getConfig().getString("messages.death_message.title");
        deathMessageSubtitle = getConfig().getString("messages.death_message.subtitle");
        isBanMessageEnabled = getConfig().getBoolean("messages.ban_message.enabled");
        banMessage = getConfig().getString("messages.ban_message.message");
    }

    private void loadGeneralConfig() {
        kickIfIpChanged = getConfig().getBoolean("config.kick_if_ip_changed");
        stormTime = getConfig().getInt("config.storm_time");
        pointsToReduceOnDeath = getConfig().getInt("config.points_to_reduce_on_death");
    }

    // Cargar grupos desde la configuración
    public List<GroupData> getGroupsFromConfig() {
        List<GroupData> groups = new ArrayList<>();
        ConfigurationSection groupsSection = getConfig().getConfigurationSection("groups");
        if (groupsSection == null) return groups;

        for (String groupName : groupsSection.getKeys(false)) {
            ConfigurationSection groupSection = groupsSection.getConfigurationSection(groupName);
            if (groupSection == null) continue;

            GroupData groupData = createGroupFromConfig(groupSection, groupName);
            groups.add(groupData);
        }
        return groups;
    }

    private GroupData createGroupFromConfig(ConfigurationSection groupSection, String groupName) {
        String prefix = groupSection.getString("prefix", "");
        List<Permission> permissions = loadPermissions(groupSection);
        List<String> players = loadPlayers(groupSection);

        return new GroupData(groupName, prefix, permissions, players);
    }

    private List<Permission> loadPermissions(ConfigurationSection groupSection) {
        List<Permission> permissions = new ArrayList<>();
        for (String perm : groupSection.getStringList("permissions")) {
            permissions.add(Permission.valueOf(perm.toUpperCase()));
        }
        return permissions;
    }

    private List<String> loadPlayers(ConfigurationSection groupSection) {
        return new ArrayList<>(groupSection.getStringList("players"));
    }

    // Acceso a la configuración de archivo
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

    // Métodos de acceso a la configuración
    public boolean isKickIfIpChanged() {
        return kickIfIpChanged;
    }

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

    public int getStormTime() {
        return stormTime;
    }

    public int getPointsToReduceOnDeath() {
        return pointsToReduceOnDeath;
    }
}
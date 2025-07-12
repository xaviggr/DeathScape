package dc.Persistence.config;

import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
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

/**
 * Singleton class that manages the server's main configuration.
 * This includes message configurations, general settings, and group management.
 */
public class MainConfigManager {

    private static MainConfigManager instance; // Singleton instance
    private DeathScape plugin; // Reference to the main plugin

    // Message Configuration
    private Boolean isWelcomeMessageEnabled;
    private String welcomeMessage;
    private String deathMessageTitle;
    private String deathMessageSubtitle;
    private String banMessage;
    private boolean isBanMessageEnabled;

    // General Configuration
    private boolean kickIfIpChanged;
    private int stormTime;
    private int pointsToReduceOnDeath;
    private int maxPlayersInWorld;

    //leaderboard config
    private String githubToken;
    private int leaderboardUpdateInterval;

    //dungeon config
    private double lootProbabilityScale;

    // Private constructor for Singleton
    private MainConfigManager() {}

    /**
     * Initializes the MainConfigManager Singleton instance and loads the configuration.
     *
     * @param plugin The main plugin instance.
     */
    public static void setInstance(DeathScape plugin) {
        if (instance == null) {
            instance = new MainConfigManager();
            instance.plugin = plugin;

            // Set up database file paths
            PlayerDatabase.setNameFile(plugin.getDataFolder() + File.separator + "players.json");
            BannedWordsDatabase.setDatabaseFile(plugin.getDataFolder() + File.separator + "bannedWords.json");
            ReportsDatabase.setReportsFile(plugin.getDataFolder() + File.separator + "reports.json");
            GroupDatabase.setNameFile(plugin.getDataFolder() + File.separator + "groups.json");
            LogDatabase.setLogFile(plugin.getDataFolder() + File.separator + "logs.json");

            // Load the configuration
            instance.loadConfig();
        }
    }

    /**
     * Retrieves the Singleton instance of MainConfigManager.
     *
     * @return The Singleton instance.
     */
    public static MainConfigManager getInstance() {
        return instance;
    }

    /**
     * Loads the configuration from the file.
     */
    public void loadConfig() {
        loadMessageConfig();
        loadGeneralConfig();
    }

    // ------------------- Message Configuration -------------------

    private void loadMessageConfig() {
        isWelcomeMessageEnabled = getConfig().getBoolean("messages.welcome_message.enabled");
        welcomeMessage = getConfig().getString("messages.welcome_message.message");
        deathMessageTitle = getConfig().getString("messages.death_message.title");
        deathMessageSubtitle = getConfig().getString("messages.death_message.subtitle");
        isBanMessageEnabled = getConfig().getBoolean("messages.ban_message.enabled");
        banMessage = getConfig().getString("messages.ban_message.message");
    }

    // ------------------- General Configuration -------------------

    private void loadGeneralConfig() {
        kickIfIpChanged = getConfig().getBoolean("config.kick_if_ip_changed");
        stormTime = getConfig().getInt("config.storm_time");
        pointsToReduceOnDeath = getConfig().getInt("config.points_to_reduce_on_death");
        maxPlayersInWorld = getConfig().getInt("config.max_players_in_world");

        githubToken = getConfig().getString("leaderboard.github_token");
        leaderboardUpdateInterval = getConfig().getInt("leaderboard.update_interval", 1);
        lootProbabilityScale = getConfig().getDouble("loot-probability-scale", 50.0);

        if (!getConfig().isSet("leaderboard.github_token")) {
            getConfig().set("leaderboard.github_token", "<token>");
        }
        if (!getConfig().isSet("leaderboard.update_interval_minutes")) {
            getConfig().set("leaderboard.update_interval_minutes", 10);
        }
    }

    // ------------------- Group Management -------------------

    /**
     * Retrieves a list of groups defined in the configuration file.
     *
     * @return A list of GroupData objects.
     */
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

    // ------------------- Configuration File Access -------------------

    private FileConfiguration fileConfiguration = null;

    /**
     * Retrieves the FileConfiguration object for accessing the configuration file.
     *
     * @return The FileConfiguration object.
     */
    public FileConfiguration getConfig() {
        if (fileConfiguration == null) {
            reloadConfig();
        }
        return fileConfiguration;
    }

    /**
     * Reloads the configuration file from disk.
     *
     * @return True if the configuration was reloaded successfully, false otherwise.
     */
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

    // ------------------- Getters for Configuration Values -------------------

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

    public int getMaxPlayersInWorld() {
        return maxPlayersInWorld;
    }

    public String getGithubToken() { return githubToken; }

    public int getLeaderboardUpdateInterval() { return leaderboardUpdateInterval; }

    public double getlootProbabilityScale() { return lootProbabilityScale; }
}
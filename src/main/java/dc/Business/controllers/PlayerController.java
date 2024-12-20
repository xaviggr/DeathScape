package dc.Business.controllers;

import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.Business.player.PlayerTabList;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.config.MainConfigManager;
import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerController {

    // Fields for player-related functionalities
    private final PlayerDeath playerDeath;
    private final PlayerTabList playerTabList;
    private final Map<UUID, Location> previousLocations = new HashMap<>();
    private final DeathScape plugin;

    // Constructor to initialize necessary components
    public PlayerController(DeathScape plugin) {
        PlayerBan playerBan = new PlayerBan(); // Initialized player ban functionality
        this.playerTabList = new PlayerTabList(plugin, this); // Initialize TabList animation
        this.playerDeath = new PlayerDeath(plugin, playerBan); // Initialize death functionality
        this.plugin = plugin;
        handleSavingPlayerData();
    }

    /**
     * Marks a player as dead and reduces their points on death.
     * @param player The player to be marked as dead.
     */
    public void setPlayerAsDead(Player player) {
        // Mark player as dead and update points
        playerDeath.Dead(player);
        int pointsToReduce = MainConfigManager.getInstance().getPointsToReduceOnDeath() * -1;
        addPointsToPlayer(player, pointsToReduce);
    }

    /**
     * Sets the player's group.
     * @param player The player to assign the group.
     * @param group The group to assign to the player.
     * @return True if the group was set successfully, otherwise false.
     */
    public boolean setGroupToPlayer(Player player, String group) {
        // Add player to specified group in the database
        return PlayerEditDatabase.addPlayerToGroup(player.getName(), group);
    }

    /**
     * Adds one point to the player's total points.
     * @param player The player to whom points will be added.
     */
    public void addPointToPlayer(Player player) {
        addPointsToPlayer(player, 1); // Calls the generic points adding method
    }

    /**
     * Adds specified points to the player.
     * Ensures that points don't go below zero.
     * @param player The player to whom points will be added.
     * @param points The number of points to add.
     */
    public void addPointsToPlayer(Player player, int points) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        // Ensure that player data exists
        if (playerData != null) {
            playerData.setPoints(Math.max(playerData.getPoints() + points, 0)); // Prevent negative points
            PlayerDatabase.addPlayerDataToDatabase(playerData); // Save updated player data
        }
    }

    /**
     * Initializes the TabList animation for the player.
     * @param player The player to set up the TabList.
     */
    public void setUpTabList(Player player) {
        playerTabList.startAnimation(player); // Start TabList animation
    }

    /**
     * Retrieves the group of the player.
     * @param player The player whose group is to be retrieved.
     * @return The group of the player.
     */
    public String getGroupFromPlayer(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        // Ensure player data exists
        if (playerData != null) {
            return playerData.getGroup(); // Return the player's group
        }
        return null; // Return null if player data doesn't exist
    }

    /**
     * Activates the Banshee effects for the player (invisibility, speed, etc.).
     * @param player The player to apply the Banshee effects to.
     */
    public void activateBanshee(Player player) {
        // Applying potion effects for Banshee mode
        addPotionEffect(player, PotionEffectType.INVISIBILITY, 1);
        addPotionEffect(player, PotionEffectType.SPEED, 1);
        addPotionEffect(player, PotionEffectType.JUMP, 3);
        addPotionEffect(player, PotionEffectType.NIGHT_VISION, 1);
        addPotionEffect(player, PotionEffectType.WATER_BREATHING, 1);

        // Allow flight and make the player invulnerable
        player.setAllowFlight(true);
        player.setInvulnerable(true);
    }

    /**
     * Deactivates the Banshee effects for the player.
     * @param player The player to remove the Banshee effects from.
     */
    public void deactivateBanshee(Player player) {
        // Remove all active potion effects related to Banshee mode
        removePotionEffect(player, PotionEffectType.INVISIBILITY);
        removePotionEffect(player, PotionEffectType.SPEED);
        removePotionEffect(player, PotionEffectType.JUMP);
        removePotionEffect(player, PotionEffectType.NIGHT_VISION);
        removePotionEffect(player, PotionEffectType.WATER_BREATHING);

        // Disable flight and invulnerability
        player.setAllowFlight(false);
        player.setInvulnerable(false);
    }

    /**
     * Checks if the Banshee effect is currently active for the player.
     * @param player The player to check.
     * @return True if Banshee is active, otherwise false.
     */
    public boolean isBansheeActive(Player player) {
        // Check if the player has the invisibility effect as an indication of Banshee mode
        return player.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    // Helper methods to avoid code duplication for potion effect application and removal
    private void addPotionEffect(Player player, PotionEffectType effectType, int level) {
        player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, level, false, false));
    }

    private void removePotionEffect(Player player, PotionEffectType effectType) {
        player.removePotionEffect(effectType);
    }

    // Método para teletransportar a un jugador a otro
    public boolean teleportToPlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            savePreviousLocation(player);
            player.teleport(target);
            return true;
        }
        return false;
    }

    // Método para teletransportar a un jugador a coordenadas
    public boolean teleportToCoordinates(Player player, double x, double y, double z) {
        savePreviousLocation(player);
        Location location = new Location(player.getWorld(), x, y, z);
        player.teleport(location);
        return true;
    }

    // Método para teletransportar a un jugador origen a otro jugador destino
    public boolean teleportPlayerToPlayer(String sourceName, String targetName) {
        Player source = Bukkit.getPlayer(sourceName);
        Player target = Bukkit.getPlayer(targetName);
        if (source != null && target != null) {
            savePreviousLocation(source);
            source.teleport(target);
            return true;
        }
        return false;
    }

    // Método para regresar a la ubicación anterior
    public boolean teleportBack(Player player) {
        if (previousLocations.containsKey(player.getUniqueId())) {
            Location previousLocation = previousLocations.get(player.getUniqueId());
            player.teleport(previousLocation);
            return true;
        }
        return false;
    }

    // Guarda la ubicación actual antes de teletransportarse
    private void savePreviousLocation(Player player) {
        previousLocations.put(player.getUniqueId(), player.getLocation());
    }

    public void setPlayerRank(Player player, String prefix) {
        // Obtener el Scoreboard
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard scoreboard = manager.getMainScoreboard();

        // Crear o encontrar el equipo del rango
        Team team = scoreboard.getTeam(prefix);
        if (team == null) {
            team = scoreboard.registerNewTeam(prefix);
            team.setPrefix(prefix + " ");
        }

        // Añadir al jugador al equipo
        team.addEntry(player.getName());

        // Asignar el Scoreboard al jugador (si es un Scoreboard personalizado)
        player.setScoreboard(scoreboard);
    }

    public void healPlayer(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
    }

    public boolean isMuted(Player player) {
        return BannedWordsDatabase.getMutedUsers().contains(player.getName());
    }

    public void mutePlayer(Player player, Player target) {
        BannedWordsDatabase.addMutedUser(target.getName());
        player.sendMessage(ChatColor.GREEN + "El jugador " + target.getName() + " ha sido silenciado.");
    }

    public void unmutePlayer(Player player, Player target) {
        BannedWordsDatabase.removeMutedUser(target.getName());
        player.sendMessage(ChatColor.GREEN + "El jugador " + target.getName() + " ha sido desilenciado.");
    }

    private void addPlayerToQueue(Player player) {
        plugin.getServerData().addPlayerToQueue(player);
    }

    private void removePlayerFromQueue(Player player) {
        plugin.getServerData().removePlayerFromQueue(player);
    }

    public void addPlayerToServer(Player player) {
        addPlayerToQueue(player);
    }

    public void removePlayerFromServer(Player player) {
        // Save player time played and coordinates if is not in spawn world
        if (!Objects.requireNonNull(player.getLocation().getWorld()).getName().equals("world_minecraft_spawn")) {
            PlayerEditDatabase.setPlayerCoords(player);
            handleTimePlayed(player);
        }
        deactivateBanshee(player);
        plugin.getServerData().decreasePlayerCounter();
        removePlayerFromQueue(player);
    }

    public void removePlayersFromServer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removePlayerFromServer(player);
        }
    }

    private void savePlayerData(Player player) {
        if (!Objects.requireNonNull(player.getLocation().getWorld()).getName().equals("world_minecraft_spawn")) {
            PlayerEditDatabase.setPlayerCoords(player);
            handleTimePlayed(player);
        }
    }

    public void savePlayersData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player);
        }
    }

    public void handleSavingPlayerData() {
        new BukkitRunnable() {
            @Override
            public void run() {
                savePlayersData();
            }
        }.runTaskTimer(plugin, 0, 6000); // 5 minutos
    }

    private void handleTimePlayed(Player player) {
        if (plugin.time_of_connection.containsKey(player.getName())) {
            long initTime = plugin.time_of_connection.get(player.getName());
            long actualTime = System.currentTimeMillis();
            long onlineTime = actualTime - initTime;

            int segundos = Integer.parseInt(String.valueOf((int) (onlineTime / 1000) % 60));
            int minutos = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60)) % 60)));
            int horas = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60 * 60)) % 24)));
            PlayerEditDatabase.setPlayerTimePlayed(player, segundos, minutos, horas);
            plugin.time_of_connection.remove(player.getName());
        }
    }

    public int getQueuePosition(Player player) {
        return plugin.getServerData().getQueuePosition(player);
    }
}

package dc.Business.controllers;

import dc.Business.player.PlayerDeath;
import dc.Business.player.PlayerTabList;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.config.MainConfigManager;
import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.*;

/**
 * Manages various player-related functionalities such as status, points, ranks,
 * teleportation, invisibility, banshee effects, and data saving for the DeathScape plugin.
 */
public class PlayerController {

    // Fields
    private final PlayerDeath playerDeath;
    private final PlayerTabList playerTabList;
    private final Map<UUID, Location> previousLocations = new HashMap<>();
    private final DeathScape plugin;

    /**
     * Constructor to initialize the PlayerController with the required plugin and subcomponents.
     *
     * @param plugin The main instance of the DeathScape plugin.
     */
    public PlayerController(DeathScape plugin) {
        this.playerTabList = new PlayerTabList(plugin, this);
        this.playerDeath = new PlayerDeath(plugin);
        this.plugin = plugin;
        handleSavingPlayerData();
    }

    // --------------------------------------
    // PLAYER STATUS AND POINTS MANAGEMENT
    // --------------------------------------

    /**
     * Sets a player as dead, updates their points, and handles their death logic.
     *
     * @param player The player to mark as dead.
     */
    public void setPlayerAsDead(Player player) {
        playerDeath.Dead(player);
        int pointsToReduce = MainConfigManager.getInstance().getPointsToReduceOnDeath() * -1;
        addPointsToPlayer(player, pointsToReduce);
    }

    /**
     * Adds a single point to the player's total.
     *
     * @param player The player to receive the point.
     */
    public void addPointToPlayer(Player player) {
        addPointsToPlayer(player, 1);
    }

    /**
     * Adds a specified number of points to the player's total.
     *
     * @param player The player to receive the points.
     * @param points The number of points to add.
     */
    public void addPointsToPlayer(Player player, int points) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData != null) {
            playerData.setPoints(Math.max(playerData.getPoints() + points, 0));
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Adds a specified number of lives to the player's total.
     *
     * @param player The player to receive the points.
     * @param lives The number of points to add.
     */
    public void addLivesToPlayer(OfflinePlayer player, int lives) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData != null) {
            playerData.setLifes(Math.max(playerData.getLifes() + lives, -1));
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    public void setLivesToPlayer(OfflinePlayer player, int lives) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData != null) {
            playerData.setLifes(lives);
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    public void removeLivesFromPlayer(Player player, int lives) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData != null) {
            playerData.setLifes(playerData.getLifes() - lives);
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
        if (playerData.getLifes() < 0) {
            // Banear al jugador
            // Update player state, weather, and start death animation
            setPlayerAsDead(player);
            player.kickPlayer("Has perdido todas tus vidas.");
        }
    }

    // --------------------------------------
    // PLAYER GROUP MANAGEMENT
    // --------------------------------------

    /**
     * Assigns a player to a specified group.
     *
     * @param player The player to assign to the group.
     * @param group  The group to assign to the player.
     * @return True if the operation succeeded, false otherwise.
     */
    public boolean setGroupToPlayer(Player player, String group) {
        return PlayerEditDatabase.addPlayerToGroup(player.getName(), group);
    }

    /**
     * Retrieves the group of a specified player.
     *
     * @param player The player to retrieve the group from.
     * @return The group of the player, or null if the player is not found.
     */
    public String getGroupFromPlayer(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        return playerData != null ? playerData.getGroup() : null;
    }

    // --------------------------------------
    // TAB LIST MANAGEMENT
    // --------------------------------------

    /**
     * Sets up a player's tab list with animations and configurations.
     *
     * @param player The player for whom the tab list is set up.
     */
    public void setUpTabList(Player player) {
        playerTabList.startAnimation(player);
    }

    // --------------------------------------
    // PLAYER RANK MANAGEMENT
    // --------------------------------------

    /**
     * Sets the rank of a player with a specified prefix.
     *
     * @param player The player to set the rank for.
     * @param prefix The prefix to assign to the player's rank.
     */
    public void setPlayerRank(Player player, String prefix) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        Scoreboard scoreboard = manager.getMainScoreboard();

        // Create a new team with the prefix
        Team team = scoreboard.getTeam(prefix);
        if (team == null) {
            team = scoreboard.registerNewTeam(prefix);
            team.setPrefix(prefix + " ");
        }

        // Add the player to the team
        team.addEntry(player.getName());

        // Assign the team to the player
        player.setScoreboard(scoreboard);
    }

    // --------------------------------------
    // BANSHEE EFFECTS
    // --------------------------------------

    /**
     * Activates banshee effects for a player, granting various abilities and making them invulnerable.
     *
     * @param player The player to activate the banshee effects for.
     */
    public void activateBanshee(Player player) {
        addPotionEffect(player, PotionEffectType.SPEED, 1);
        addPotionEffect(player, PotionEffectType.JUMP, 3);
        addPotionEffect(player, PotionEffectType.NIGHT_VISION, 1);
        addPotionEffect(player, PotionEffectType.WATER_BREATHING, 1);
        addInvisiblePlayer(player);
        player.setAllowFlight(true);
        player.setInvulnerable(true);
    }

    /**
     * Deactivates banshee effects for a player, removing abilities and invulnerability.
     *
     * @param player The player to deactivate the banshee effects for.
     */
    public void deactivateBanshee(Player player) {
        removePotionEffect(player, PotionEffectType.SPEED);
        removePotionEffect(player, PotionEffectType.JUMP);
        removePotionEffect(player, PotionEffectType.NIGHT_VISION);
        removePotionEffect(player, PotionEffectType.WATER_BREATHING);
        removeInvisiblePlayer(player);
        player.setAllowFlight(false);
        player.setInvulnerable(false);
    }

    /**
     * Checks if banshee effects are active for a player.
     *
     * @param player The player to check.
     * @return True if banshee effects are active, false otherwise.
     */
    public boolean isBansheeActive(Player player) {
        return isPlayerInvisible(player) && player.getAllowFlight() && player.isInvulnerable();
    }

    /**
     * Adds a potion effect to a player with a specified level.
     *
     * @param player     The player to add the potion effect to.
     * @param effectType The type of potion effect to add.
     * @param level      The level of the potion effect.
     */
    private void addPotionEffect(Player player, PotionEffectType effectType, int level) {
        player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, level, false, false));
    }

    /**
     * Removes a potion effect from a player.
     *
     * @param player     The player to remove the potion effect from.
     * @param effectType The type of potion effect to remove.
     */
    private void removePotionEffect(Player player, PotionEffectType effectType) {
        player.removePotionEffect(effectType);
    }

    // --------------------------------------
    // TELEPORTATION
    // --------------------------------------

    /**
     * Teleports a player to another player's location.
     *
     * @param player      The player to teleport.
     * @param targetName  The name of the target player to teleport to.
     * @return True if the teleportation was successful, false otherwise.
     */
    public boolean teleportToPlayer(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target != null) {
            savePreviousLocation(player);
            player.teleport(target);
            return true;
        }
        return false;
    }

    /**
     * Teleports a player to specific coordinates in the world.
     *
     * @param player The player to teleport.
     * @param x      The x-coordinate to teleport to.
     * @param y      The y-coordinate to teleport to.
     * @param z      The z-coordinate to teleport to.
     * @return True if the teleportation was successful, false otherwise.
     */
    public boolean teleportToCoordinates(Player player, double x, double y, double z) {
        savePreviousLocation(player);
        Location location = new Location(player.getWorld(), x, y, z);
        player.teleport(location);
        return true;
    }

    /**
     * Teleports a player to another player's location.
     *
     * @param sourceName The name of the player to teleport.
     * @param targetName The name of the target player to teleport to.
     * @return True if the teleportation was successful, false otherwise.
     */
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

    /**
     * Teleports a player back to their previous location.
     *
     * @param player The player to teleport back.
     * @return True if the teleportation was successful, false otherwise.
     */
    public boolean teleportBack(Player player) {
        if (previousLocations.containsKey(player.getUniqueId())) {
            Location previousLocation = previousLocations.get(player.getUniqueId());
            player.teleport(previousLocation);
            return true;
        }
        return false;
    }

    /**
     * Saves the previous location of a player before teleporting.
     *
     * @param player The player to save the location for.
     */
    private void savePreviousLocation(Player player) {
        previousLocations.put(player.getUniqueId(), player.getLocation());
    }

    // --------------------------------------
    // PLAYER VISIBILITY
    // --------------------------------------

    /**
     * The set of invisible players.
     */
    private final Set<UUID> invisiblePlayers = new HashSet<>(); // List of invisible players

    /**
     * Apply the invisibility effect to a player.
     * @param player The player to apply the effect to.
     */
    public void applyInvisibilityToPlayer(Player player) {
        for (Player invisiblePlayer : Bukkit.getOnlinePlayers()) {
            if (invisiblePlayers.contains(invisiblePlayer.getUniqueId()) && !player.isOp()) {
                // Hide invisible players for non-OP players
                player.hidePlayer(plugin, invisiblePlayer);
            } else {
                // Show players to OPs or if they are not invisible
                player.showPlayer(plugin, invisiblePlayer);
            }
        }
    }

    /**
     * Update the visibility of all players based on the invisible players list.
     */
    public void updateInvisibilityForAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (Player invisiblePlayer : Bukkit.getOnlinePlayers()) {
                if (invisiblePlayers.contains(invisiblePlayer.getUniqueId()) && !player.isOp()) {
                    // Hide invisible players for non-OP players
                    player.hidePlayer(plugin, invisiblePlayer);
                } else {
                    // Show players to OPs or if they are not invisible
                    player.showPlayer(plugin, invisiblePlayer);
                }
            }
        }
    }

    /**
     * Add a player to the invisible players list and update visibility for all.
     * @param player The player to make invisible.
     */
    public void addInvisiblePlayer(Player player) {
        if (invisiblePlayers.add(player.getUniqueId())) {
            // Update visibility for all players
            updateInvisibilityForAll();
        }
    }

    /**
     * Delete a player from the invisible players list and update visibility for all.
     * @param player The player to make visible.
     */
    public void removeInvisiblePlayer(Player player) {
        if (invisiblePlayers.remove(player.getUniqueId())) {
            // Update visibility for all players
            updateInvisibilityForAll();
        }
    }

    /**
     * Verify if a player is invisible.
     * @param player The player to check.
     * @return True if the player is invisible, false otherwise.
     */
    public boolean isPlayerInvisible(Player player) {
        return invisiblePlayers.contains(player.getUniqueId());
    }

    // --------------------------------------
    // PLAYER HEALTH AND MUTING
    // --------------------------------------

    /**
     * Heals a player to full health, food, and saturation.
     *
     * @param player The player to heal.
     */
    public void healPlayer(Player player) {
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
    }

    /**
     * Checks if a player is muted.
     * @param player The player to check.
     * @return True if the player is muted, false otherwise.
     */
    public boolean isMuted(Player player) {
        return BannedWordsDatabase.isUserMuted(player.getName());
    }

    /**
     * Mutes a player, preventing them from sending messages in chat.
     *
     * @param player The player who wants to be muted.
     * @param target The player to mute.
     * @param muteDurationInMinutes The duration of the mute in minutes.
     */
    public void mutePlayer(Player player, Player target, long muteDurationInMinutes) {
        long muteTime = System.currentTimeMillis() + (muteDurationInMinutes * 60 * 1000);
        BannedWordsDatabase.addMutedUser(target.getName(), muteTime);
        player.sendMessage(ChatColor.GREEN + "El jugador " + target.getName() + " ha sido silenciado por " + muteDurationInMinutes + " minutos.");
    }

    /**
     * Unmutes a player, allowing them to send messages in chat.
     *
     * @param player The player who wants to unmute.
     * @param target The player to unmute.
     */
    public void unmutePlayer(Player player, Player target) {
        BannedWordsDatabase.removeMutedUser(target.getName());
        player.sendMessage(ChatColor.GREEN + "El jugador " + target.getName() + " ha sido desilenciado.");
    }

    // --------------------------------------
    // SERVER MANAGEMENT
    // --------------------------------------

    /**
     * Adds a player to the server queue.
     *
     * @param player The player to add to the queue.
     */
    public void addPlayerToServer(Player player) {
        addPlayerToQueue(player);
    }

    /**
     * Removes a player from the server, updating their data and removing them from the queue.
     *
     * @param player The player to remove from the server.
     */
    public void removePlayerFromServer(Player player) {
        if (!Objects.requireNonNull(player.getLocation().getWorld()).getName().equals("world_minecraft_spawn")) {
            PlayerEditDatabase.setPlayerCoords(player);
            handleTimePlayedAndRemove(player);
            plugin.getServerData().decreasePlayerCounter();
            removePlayerFromQueue(player);
        }
        deactivateBanshee(player);
    }

    /**
     * Removes all players from the server, updating their data and removing them from the queue.
     */
    public void removePlayersFromServer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            removePlayerFromServer(player);
        }
    }

    /**
     * Adds a player to the server queue.
     *
     * @param player The player to add to the queue.
     */
    private void addPlayerToQueue(Player player) {
        plugin.getServerData().addPlayerToQueue(player);
    }

    /**
     * Removes a player from the server queue.
     *
     * @param player The player to remove from the queue.
     */
    private void removePlayerFromQueue(Player player) {
        plugin.getServerData().removePlayerFromQueue(player);
    }

    // --------------------------------------
    // PLAYER DATA SAVING
    // --------------------------------------

    /**
     * Saves the data of a player to the database.
     *
     * @param player The player to save the data for.
     */
    public void savePlayerData(Player player) {
        if (!Objects.requireNonNull(player.getLocation().getWorld()).getName().equals("world_minecraft_spawn")) {
            PlayerEditDatabase.setPlayerCoords(player);
            PlayerEditDatabase.setPlayerHealth(player);
            updateTimePlayed(player);
        }
    }

    /**
     * Saves the data of all online players to the database.
     */
    public void savePlayersData() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            savePlayerData(player);
        }
    }

    /**
     * Handles the saving of player data at regular intervals.
     */
    public void handleSavingPlayerData() {
        new BukkitRunnable() {
            @Override
            public void run() {
                savePlayersData();
            }
        }.runTaskTimer(plugin, 0, 6000);
    }

    // --------------------------------------
    // TIME PLAYED MANAGEMENT
    // --------------------------------------

    /**
     * Updates the time played for a player.
     *
     * @param player The player to update the time played for.
     */
    public void updateTimePlayed(Player player) {
        calculateAndUpdateTime(player, false);
    }

    /**
     * Handles the time played for a player and removes them from the time tracking map.
     *
     * @param player The player to handle the time played for.
     */
    public void handleTimePlayedAndRemove(Player player) {
        calculateAndUpdateTime(player, true);
    }

    /**
     * Calculates and updates the time played for a player.
     *
     * @param player           The player to calculate the time played for.
     * @param removeAfterUpdate True if the player should be removed from the time tracking map after updating.
     */
    private void calculateAndUpdateTime(Player player, boolean removeAfterUpdate) {
        if (plugin.time_of_connection.containsKey(player.getName())) {
            long initTime = plugin.time_of_connection.get(player.getName());
            long actualTime = System.currentTimeMillis();
            long onlineTime = actualTime - initTime;

            int segundos = (int) ((onlineTime / 1000) % 60);
            int minutos = (int) ((onlineTime / (1000 * 60)) % 60);
            int horas = (int) ((onlineTime / (1000 * 60 * 60)));

            PlayerEditDatabase.setPlayerTimePlayed(player, segundos, minutos, horas);

            if (removeAfterUpdate) {
                plugin.time_of_connection.remove(player.getName());
            } else {
                plugin.time_of_connection.put(player.getName(), actualTime);
            }
        }
    }

    /**
     * Retrieves position in the server queue for a player.
     *
     * @param player The player to get the queue position for.
     * @return The position in the queue for the player.
     */
    public int getQueuePosition(Player player) {
        return plugin.getServerData().getQueuePosition(player);
    }
}

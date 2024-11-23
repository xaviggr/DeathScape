package dc.Business.controllers;

import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.Business.player.PlayerTabList;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerController {

    // Fields for player-related functionalities
    private final PlayerDeath playerDeath;
    private final PlayerTabList playerTabList;

    // Constructor to initialize necessary components
    public PlayerController(DeathScape plugin) {
        PlayerBan playerBan = new PlayerBan(); // Initialized player ban functionality
        this.playerTabList = new PlayerTabList(plugin, this); // Initialize TabList animation
        this.playerDeath = new PlayerDeath(plugin, playerBan); // Initialize death functionality
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
     * Removes the group from the player.
     * @param player The player whose group is to be removed.
     * @return True if the group was removed successfully, otherwise false.
     */
    public boolean removeGroupFromPlayer(Player player) {
        // Remove player from any existing group
        return PlayerEditDatabase.removePlayerFromGroup(player.getName());
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
}

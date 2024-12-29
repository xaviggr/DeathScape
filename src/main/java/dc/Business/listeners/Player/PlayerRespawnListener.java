package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Objects;

/**
 * Listener for handling player respawn events. Manages respawn locations and bans players
 * who are not admins after their death.
 */
public class PlayerRespawnListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    /**
     * Constructs a `PlayerRespawnListener` with the required plugin and player controller.
     *
     * @param playerController The controller managing player-related functionality.
     * @param plugin           The instance of the DeathScape plugin.
     */
    public PlayerRespawnListener(PlayerController playerController, DeathScape plugin) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    /**
     * Handles the `PlayerRespawnEvent` triggered when a player respawns.
     * Sets a custom respawn location and handles banning players who are not admins.
     *
     * @param event The `PlayerRespawnEvent` triggered when a player respawns.
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        boolean banned = PlayerEditDatabase.isPlayerBanned(player.getName());
        System.out.println("PlayerRespawnListener: Player " + player.getName() + " is banned: " + banned);

        // Handle banned player case after respawn
        if (banned) {
            // Coordinates for the respawn location
            double x = 4104.557; // Change these coordinates based on your server's world
            double y = 53.5;
            double z = -547.535;

            // Name of the world where the player will respawn
            String worldName = "world"; // Update this if your world has a different name

            // Create the respawn location
            Location respawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

            // Set the respawn location for normal deaths
            event.setRespawnLocation(respawnLocation);

            // Handle admin case: set to spectator mode
            if (!player.isOp()) {
                handlePlayerBan(player);
            }
        } else {
            event.setRespawnLocation(Objects.requireNonNull(Bukkit.getWorld("world")).getSpawnLocation());
        }
    }

    /**
     * Bans a player after they respawn if they are not an admin.
     *
     * @param player The player to be banned.
     */
    private void handlePlayerBan(Player player) {
        if (player.isOnline()) {
            // Schedule the ban 1 second after the respawn to ensure it happens after the player has fully respawned
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.kickPlayer(ChatColor.RED + MainConfigManager.getInstance().getBanMessage()), 20L); // 20 ticks = 1 second
        }
    }
}
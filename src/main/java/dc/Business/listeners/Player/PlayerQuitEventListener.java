package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for handling player quit events. Tracks player disconnections and applies bans
 * if the player disconnects with low health after receiving recent damage.
 */
public class PlayerQuitEventListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    /**
     * Constructs a `PlayerQuitEventListener` with the required plugin and player controller.
     *
     * @param plugin           The instance of the DeathScape plugin.
     * @param playerController The controller managing player-related functionality.
     */
    public PlayerQuitEventListener(DeathScape plugin, PlayerController playerController) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    /**
     * Handles the `PlayerQuitEvent` triggered when a player disconnects from the server.
     * Checks if the player should be banned for combat logging with low health and saves player data.
     *
     * @param event The `PlayerQuitEvent` triggered when a player disconnects.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if the player has low health and has recently taken damage
        double healthThreshold = 4.0; // Threshold for low health in hearts
        if (player.getHealth() <= healthThreshold && PlayerDamageListener.hasReceivedRecentDamage(player) && !player.isDead()) {
            // Notify all players about the ban
            Bukkit.broadcastMessage(ChatColor.RED + "El jugador " + player.getName() + " ha sido baneado por desconectarse tras recibir daño con poca vida.");

            // Handle the player's ban
            handlePlayerBan(player);
        }

        // Remove the player from the server and save their data
        playerController.removePlayerFromServer(player);
        playerController.savePlayerData(player);
    }

    /**
     * Bans a player for disconnecting with low health after receiving recent damage.
     *
     * @param player The player to ban.
     */
    private void handlePlayerBan(Player player) {
        // Ban the player
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                player.getName(),
                "Desconexión en combate con poca vida",
                null,
                "console"
        );

        // Immediately kick the player
        player.kickPlayer("Has sido baneado por desconectarte con poca vida.");
    }
}
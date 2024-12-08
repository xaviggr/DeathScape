package dc.Business.listeners;

import dc.Business.controllers.PlayerController;
import dc.Business.inventory.ReportInventory;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.Persistence.groups.GroupDatabase;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;
import java.util.Objects;

public class ChatListener implements Listener {

    private final PlayerController playerController;

    // Constructor
    public ChatListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    /**
     * Handles player chat events. Processes reports and banned word detection.
     * @param event The AsyncPlayerChatEvent triggered when a player sends a message.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check if the player is in the process of making a report
        if (isPendingReport(player)) {
            handlePlayerReport(event, player, message);
            return; // Exit after handling the report
        }

        // Check for banned words in the message
        if (containsBannedWords(message)) {
            handleBannedWords(event, player);
            return; // Exit after handling the banned words
        }

        // Add group prefix and color to the message
        String group = playerController.getGroupFromPlayer(player);
        String prefix = ChatColor.translateAlternateColorCodes(
                '&',
                Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(group))
        );

        // Format: Prefix only colored, message follows in default color
        String formattedMessage = String.format(
                "%s%s %s%s: %s",
                prefix,
                ChatColor.RESET, // Reset colors after the prefix
                ChatColor.WHITE, // Optional: Add white color to name for clarity
                player.getDisplayName(),
                message
        );

        event.setFormat(formattedMessage);
    }

    /**
     * Checks if the player is currently in the process of reporting another player.
     * @param player The player sending the chat message.
     * @return True if the player is reporting, otherwise false.
     */
    private boolean isPendingReport(Player player) {
        return ReportInventory.getPendingReports().containsKey(player);
    }

    /**
     * Handles the report comment from the player and sends it to the database.
     * @param event The chat event.
     * @param player The player making the report.
     * @param comment The comment from the player about the report.
     */
    private void handlePlayerReport(AsyncPlayerChatEvent event, Player player, String comment) {
        event.setCancelled(true);  // Cancel the chat message to prevent it from being sent to others

        Map<Player, String> pendingReports = ReportInventory.getPendingReports();
        String reportedPlayerName = pendingReports.get(player);  // Get the name of the player being reported

        sendReport(player, reportedPlayerName, comment);  // Send the report
        Message.enviarMensajeColorido(player, "Gracias por tu reporte.", ChatColor.GREEN);  // Acknowledge the report
        pendingReports.remove(player);  // Remove the player from the pending reports list
    }

    /**
     * Sends the report to the database for processing.
     * @param reporter The player making the report.
     * @param reportedPlayerName The name of the player being reported.
     * @param comment The comment or reason for the report.
     */
    private void sendReport(Player reporter, String reportedPlayerName, String comment) {
        ReportsDatabase.addReport(reporter.getName(), reportedPlayerName, comment);  // Save the report to the database
    }

    /**
     * Checks if the message contains any banned words.
     * @param message The message to check.
     * @return True if the message contains a banned word, otherwise false.
     */
    private boolean containsBannedWords(String message) {
        for (String bannedWord : BannedWordsDatabase.getBannedWords()) {
            if (message.toLowerCase().contains(bannedWord)) {  // Case-insensitive check for banned words
                return true;
            }
        }
        return false;  // No banned words found
    }

    /**
     * Handles a message containing banned words by cancelling the event and notifying the player.
     * @param event The chat event.
     * @param player The player who sent the message.
     */
    private void handleBannedWords(AsyncPlayerChatEvent event, Player player) {
        event.setCancelled(true);  // Cancel the chat message
        Message.enviarMensajeColorido(player, "Por favor, no uses palabras prohibidas.", ChatColor.RED);  // Notify the player
    }
}

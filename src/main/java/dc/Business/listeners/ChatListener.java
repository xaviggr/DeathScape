package dc.Business.listeners;

import dc.Business.controllers.PlayerController;
import dc.Business.inventory.ReportInventory;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.Persistence.groups.GroupDatabase;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Listener for handling chat messages. Includes spam protection, banned word filtering,
 * report handling, and custom formatting with group prefixes.
 */
public class ChatListener implements Listener {

    private static final int SPAM_TIME_LIMIT = 2000; // Time limit between messages in milliseconds (2 seconds)
    private static final int MESSAGE_HISTORY_LIMIT = 2; // Maximum number of messages in history
    private static final int MESSAGE_EXPIRATION_TIME = 15000; // Expiration time for old messages in milliseconds (15 seconds)

    private final PlayerController playerController;
    private final DeathScape plugin;

    private final Map<Player, List<String>> playerMessages = new HashMap<>();
    private final Map<Player, List<Long>> messageTimestamps = new HashMap<>();
    private final Map<Player, Long> lastMessageTimes = new HashMap<>();

    /**
     * Constructs a `ChatListener` with the required player controller and plugin instance.
     *
     * @param playerController The controller managing player-related functionality.
     * @param plugin           The instance of the DeathScape plugin.
     */
    public ChatListener(PlayerController playerController, DeathScape plugin) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    /**
     * Handles player chat messages. Includes checks for spam, repeated messages,
     * banned words, pending reports, and mutes.
     *
     * @param event The `AsyncPlayerChatEvent` triggered when a player sends a chat message.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Check for spam and cancel if necessary
        if (isSpamming(player) && !player.isOp()) {
            cancelChat(event, player, "Estás escribiendo demasiado rápido. Por favor, reduce la velocidad.");
            return;
        }

        // Check for repeated messages and cancel if necessary
        if (isRepeatedMessage(player, message) && !player.isOp()) {
            cancelChat(event, player, "No puedes enviar el mismo mensaje repetidamente.");
            return;
        }

        // Save the message and its timestamp
        saveMessage(player, message);

        // Clean up old messages
        startCleanupTask(player);

        // Handle reports if the player is reporting another player
        if (isPendingReport(player)) {
            handlePlayerReport(event, player, message);
            return;
        }

        // Check for banned words and cancel if necessary
        if (containsBannedWords(message)) {
            handleBannedWords(event, player);
            return;
        }

        // Check if the player is muted and cancel if necessary
        if (playerController.isMuted(player)) {
            cancelChat(event, player, "Estás silenciado. No puedes enviar mensajes.");
            return;
        }

        // Format and send the chat message
        formatAndSendMessage(event, player, message);
    }

    /**
     * Cancels the player's chat message and sends them a reason for the cancellation.
     *
     * @param event  The chat event being handled.
     * @param player The player whose message is being canceled.
     * @param reason The reason for canceling the message.
     */
    private void cancelChat(AsyncPlayerChatEvent event, Player player, String reason) {
        event.setCancelled(true);
        Message.sendMessage(player, reason, ChatColor.RED);
    }

    /**
     * Saves the player's chat message and its timestamp for spam and repeat checks.
     *
     * @param player  The player who sent the message.
     * @param message The message sent by the player.
     */
    private void saveMessage(Player player, String message) {
        playerMessages.computeIfAbsent(player, k -> new ArrayList<>()).add(message);
        messageTimestamps.computeIfAbsent(player, k -> new ArrayList<>()).add(System.currentTimeMillis());

        // Limit the message history size
        List<String> messages = playerMessages.get(player);
        List<Long> timestamps = messageTimestamps.get(player);
        while (messages.size() > MESSAGE_HISTORY_LIMIT) {
            messages.remove(0);
            timestamps.remove(0);
        }
    }

    /**
     * Starts a cleanup task to remove old messages and timestamps.
     *
     * @param player The player whose message history is being cleaned.
     */
    private void startCleanupTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Long> timestamps = messageTimestamps.get(player);
                List<String> messages = playerMessages.get(player);
                if (timestamps == null || messages == null) return;

                long now = System.currentTimeMillis();
                timestamps.removeIf(timestamp -> now - timestamp > MESSAGE_EXPIRATION_TIME);
                messages.removeIf(msg -> timestamps.isEmpty());
            }
        }.runTaskLater(plugin, 20L); // Run after 1 second
    }

    /**
     * Checks if the player is sending messages too quickly (spamming).
     *
     * @param player The player to check for spam.
     * @return True if the player is spamming, false otherwise.
     */
    private boolean isSpamming(Player player) {
        long now = System.currentTimeMillis();
        long lastMessageTime = lastMessageTimes.getOrDefault(player, 0L);

        if (now - lastMessageTime < SPAM_TIME_LIMIT) {
            return true;
        }

        lastMessageTimes.put(player, now);
        return false;
    }

    /**
     * Checks if the player's message is repeated.
     *
     * @param player  The player to check for repeated messages.
     * @param message The message to check.
     * @return True if the message is repeated, false otherwise.
     */
    private boolean isRepeatedMessage(Player player, String message) {
        List<String> recentMessages = playerMessages.getOrDefault(player, Collections.emptyList());
        return recentMessages.stream().anyMatch(msg -> msg.equalsIgnoreCase(message));
    }

    /**
     * Checks if the player is in the process of reporting another player.
     *
     * @param player The player to check for a pending report.
     * @return True if the player is reporting, false otherwise.
     */
    private boolean isPendingReport(Player player) {
        return ReportInventory.getPendingReports().containsKey(player);
    }

    /**
     * Handles the player's report submission.
     *
     * @param event    The chat event being handled.
     * @param player   The player submitting the report.
     * @param comment  The report comment provided by the player.
     */
    private void handlePlayerReport(AsyncPlayerChatEvent event, Player player, String comment) {
        event.setCancelled(true);
        String reportedPlayer = ReportInventory.getPendingReports().get(player);
        sendReport(player, reportedPlayer, comment);
        ReportInventory.getPendingReports().remove(player);
        Message.sendMessage(player, "Gracias por tu reporte.", ChatColor.GREEN);
    }

    /**
     * Submits the report to the database.
     *
     * @param reporter      The player submitting the report.
     * @param reportedPlayer The player being reported.
     * @param comment       The comment provided with the report.
     */
    private void sendReport(Player reporter, String reportedPlayer, String comment) {
        ReportsDatabase.addReport(reporter.getName(), reportedPlayer, comment);
    }

    /**
     * Checks if the message contains banned words.
     *
     * @param message The message to check.
     * @return True if the message contains banned words, false otherwise.
     */
    private boolean containsBannedWords(String message) {
        return BannedWordsDatabase.getBannedWords().stream()
                .anyMatch(bannedWord -> message.toLowerCase().contains(bannedWord.toLowerCase()));
    }

    /**
     * Handles chat messages containing banned words.
     *
     * @param event  The chat event being handled.
     * @param player The player who sent the message.
     */
    private void handleBannedWords(AsyncPlayerChatEvent event, Player player) {
        cancelChat(event, player, "Por favor, no uses palabras prohibidas.");
    }

    /**
     * Formats the player's chat message with their group prefix and sends it to the server.
     *
     * @param event   The chat event being handled.
     * @param player  The player who sent the message.
     * @param message The player's message.
     */
    private void formatAndSendMessage(AsyncPlayerChatEvent event, Player player, String message) {
        // Escape any "%" in the player's message
        message = message.replace("%", "%%");

        // Get the group prefix
        String group = playerController.getGroupFromPlayer(player);
        String prefix = ChatColor.translateAlternateColorCodes(
                '&',
                Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(group))
        );

        // Configure the formatted message
        String formattedMessage = String.format(
                "%s%s %s%s: %s",
                ChatColor.BOLD + prefix, // Bold group prefix
                ChatColor.RESET,         // Reset colors after prefix
                ChatColor.WHITE,         // White player name
                player.getDisplayName(),
                message                  // Player's message
        );

        // Set the formatted message for the chat event
        event.setFormat(formattedMessage);
    }
}
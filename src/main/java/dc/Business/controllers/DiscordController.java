package dc.Business.controllers;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

/**
 * Handles sending messages to a Discord webhook to notify events such as player deaths in the DeathScape plugin.
 */
public class DiscordController {

    /**
     * The URL of the Discord webhook.
     */
    private final String webhookURL;

    /**
     * Constructs a DiscordController with a specified webhook URL.
     *
     * @param webhook The URL of the Discord webhook.
     */
    public DiscordController(String webhook) {
        this.webhookURL = webhook;
    }

    /**
     * Creates a JSON-formatted Discord message for a player's death event.
     *
     * <p>The message includes details such as the player's name, the world they were in,
     * their coordinates, the cause of death, and a timestamp.</p>
     *
     * @param playerName The name of the player who died.
     * @param worldName  The name of the world where the player died.
     * @param location   The location where the player died.
     * @param deathCause The cause of the player's death.
     * @return A JSON string formatted to be sent to the Discord webhook.
     */
    public String createDiscordMessage(String playerName, String worldName, Location location, String deathCause) {
        // Get the current timestamp in UNIX seconds
        long timestamp = Instant.now().getEpochSecond();

        // Format the message in a JSON structure for Discord
        return """
        {
            "embeds": [{
                "title": "DeathScape 4",
                "description": "**¡%s ha muerto!**\\n🌍 En el %s",
                "color": 14177041,
                "fields": [
                    {
                        "name": "Fecha:",
                        "value": "🕒 [ <t:%d:R> ]",
                        "inline": true
                    },
                    {
                        "name": "Causa:",
                        "value": "💀 %s",
                        "inline": true
                    },
                    {
                        "name": "Coordenadas:",
                        "value": "📍 [%s]\\nX: %d | Y: %d | Z: %d",
                        "inline": true
                    }
                ],
                "thumbnail": {
                    "url": "https://mc-heads.net/avatar/%s"
                }
            }]
        }
        """.formatted(
                playerName,                             // Player's name
                worldName,                              // World name
                timestamp,                              // Timestamp for Discord
                deathCause,                             // Cause of death
                worldName,                              // World name for the coordinates field
                (int) location.getX(),                 // X coordinate
                (int) location.getY(),                 // Y coordinate
                (int) location.getZ(),                 // Z coordinate
                playerName                              // Player's name for avatar URL
        );
    }

    /**
     * Sends a JSON payload to the configured Discord webhook.
     *
     * <p>This method establishes a connection to the Discord webhook URL, sends the JSON payload,
     * and logs the success or failure of the operation.</p>
     *
     * @param jsonPayload The JSON message to send to the Discord webhook.
     */
    public void sendWebhookMessage(String jsonPayload) {
        try {
            // Open a connection to the Discord webhook URL
            URL url = new URL(webhookURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configure the webhook connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the JSON payload to the webhook
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonPayload.getBytes());
            outputStream.flush();
            outputStream.close();

            // Get the response code from the webhook and log the result
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                Bukkit.getLogger().info("[DeathScape] Message successfully sent to the webhook.");
            } else {
                Bukkit.getLogger().warning("[DeathScape] Failed to send message to the webhook. Response code: " + responseCode);
            }
        } catch (Exception e) {
            // Log any exceptions that occur during the process
            Bukkit.getLogger().warning("[DeathScape] Error sending message to the webhook: " + e.getMessage());
        }
    }
}

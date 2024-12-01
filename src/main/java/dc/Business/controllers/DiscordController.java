package dc.Business.controllers;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;

public class DiscordController {

    private final String webhookURL;

    public DiscordController(String webhook){
        this.webhookURL=webhook;
    }
    public String createDiscordMessage(String playerName, String worldName, Location location, String deathCause) {
        // Obtén el timestamp actual en segundos UNIX
        long timestamp = Instant.now().getEpochSecond();

        // Formatea el mensaje en Discord con la estructura que especificaste
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
                playerName,                             // Nombre del jugador
                worldName,                              // Nombre del mundo
                timestamp,                              // Marca de tiempo para Discord
                deathCause,
                worldName,
                (int) location.getX(),                 // Coordenada X
                (int) location.getY(),                 // Coordenada Y
                (int) location.getZ(),                 // Coordenada Z
                playerName                              // URL de la skin basada en el nombre
        );
    }
    public void sendWebhookMessage(String jsonPayload) {
        try {
            URL url = new URL(webhookURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Configuración del webhook
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Enviar datos al webhook
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonPayload.getBytes());
            outputStream.flush();
            outputStream.close();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                Bukkit.getLogger().info("[DeathScape] Mensaje enviado al webhook correctamente.");
            } else {
                Bukkit.getLogger().warning("[DeathScape] Error enviando mensaje al webhook. Código: " + responseCode);
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("[DeathScape] Error enviando mensaje al webhook: " + e.getMessage());
        }
    }
}

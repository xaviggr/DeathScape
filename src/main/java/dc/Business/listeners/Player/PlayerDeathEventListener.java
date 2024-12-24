package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.WeatherController;
import dc.Business.controllers.AnimationController;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import dc.Business.controllers.DiscordController;

public class PlayerDeathEventListener implements Listener {

    private final PlayerController playerController;
    private final WeatherController weatherController;
    private final AnimationController animationController;
    private final String webhookURL = "https://discord.com/api/webhooks/1312191406810468363/K6Qg0h3FtcxtKpFvxeqcl4d915Z3oWpOoFw6S1xpvcZOGkCjdZ_QCk7UvnvNlSnQ9nV9";
    DiscordController discordController = new DiscordController(webhookURL);

    // Mapeo de nombres de mundos
    private final Map<String, String> worldNameMap = new HashMap<>();

    public PlayerDeathEventListener(PlayerController playerController, WeatherController weatherController, AnimationController animationController) {
        this.playerController = playerController;
        this.weatherController = weatherController;
        this.animationController = animationController;

        // Inicialización del mapa de nombres de mundos
        worldNameMap.put("world", "Overworld");
        worldNameMap.put("world_nether", "Nether");
        worldNameMap.put("world_the_end", "End");
        worldNameMap.put("world_minecraft_rift", "Rift");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());
        playerController.setPlayerAsDead(player);
        weatherController.updateStormOnPlayerDeath();
        animationController.startDeathAnimation(player);

        player = event.getEntity();
        Player killer = player.getKiller(); // Puede ser null si no hay asesino

        // Detalles de la muerte
        String playerName = player.getName();
        String worldName = player.getWorld().getName();
        Location location = player.getLocation();
        String deathCause = event.getDeathMessage();
        if (deathCause == null) {
            deathCause = "Unknown cause"; // Mensaje por defecto si es nulo
        } else {
            // Eliminar colores y formatos (caracteres como §b, §r, etc.)
            deathCause = deathCause.replaceAll("§[0-9a-fk-or]", "");

            // Eliminar prefijos específicos del formato [Warrior]
            deathCause = deathCause.replaceAll("\\[[^]]*] ", "");
        }

        // Cambiar el nombre del mundo a algo más amigable
        worldName = worldNameMap.getOrDefault(worldName, worldName); // Si no está en el mapa, usar el original

        // Mensaje de Discord
        String jsonPayload = discordController.createDiscordMessage(playerName, worldName, location, deathCause);

        // Enviar mensaje al webhook
        discordController.sendWebhookMessage(jsonPayload);
    }
}

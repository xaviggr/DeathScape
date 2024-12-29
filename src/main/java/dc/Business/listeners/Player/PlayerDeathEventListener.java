package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.WeatherController;
import dc.Business.controllers.AnimationController;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import dc.Business.controllers.DiscordController;

/**
 * Listener for handling player death events. Integrates with multiple controllers
 * to manage player state, weather effects, animations, and Discord notifications.
 */
public class PlayerDeathEventListener implements Listener {

    private final PlayerController playerController;
    private final WeatherController weatherController;
    private final AnimationController animationController;

    /**
     * Discord webhook URL for sending death notifications.
     */
    private final String webhookURL = "https://discord.com/api/webhooks/1312191406810468363/K6Qg0h3FtcxtKpFvxeqcl4d915Z3oWpOoFw6S1xpvcZOGkCjdZ_QCk7UvnvNlSnQ9nV9";
    private final DiscordController discordController = new DiscordController(webhookURL);

    /**
     * A map to translate internal world names into user-friendly names.
     */
    private final Map<String, String> worldNameMap = new HashMap<>();

    /**
     * Constructs a `PlayerDeathEventListener` and initializes its dependencies.
     *
     * @param playerController    The controller managing player-related functionality.
     * @param weatherController   The controller managing weather-related functionality.
     * @param animationController The controller managing death animations.
     */
    public PlayerDeathEventListener(PlayerController playerController, WeatherController weatherController, AnimationController animationController) {
        this.playerController = playerController;
        this.weatherController = weatherController;
        this.animationController = animationController;

        // Initialize the world name mapping
        worldNameMap.put("world", "Overworld");
        worldNameMap.put("world_nether", "Nether");
        worldNameMap.put("world_the_end", "End");
        worldNameMap.put("world_minecraft_rift", "Rift");
    }

    /**
     * Handles the `PlayerDeathEvent` triggered when a player dies.
     * Updates player state, triggers weather effects, starts death animations,
     * and sends a notification to Discord.
     *
     * @param event The `PlayerDeathEvent` triggered on player death.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());

        // Extract death details
        String playerName = player.getName();
        String worldName = player.getWorld().getName();
        Location location = player.getLocation();
        String deathCause = event.getDeathMessage();

        if (deathCause == null) {
            deathCause = "Unknown cause"; // Default message if null
        } else {
            // Remove color codes and formatting (e.g., §b, §r, etc.)
            deathCause = deathCause.replaceAll("§[0-9a-fk-or]", "");

            // Remove specific prefixes like [Warrior]
            deathCause = deathCause.replaceAll("\\[[^]]*] ", "");
        }

        // Convert internal world name to user-friendly name
        worldName = worldNameMap.getOrDefault(worldName, worldName); // Use original if not in the map

        // Create and send Discord notification
        String jsonPayload = discordController.createDiscordMessage(playerName, worldName, location, deathCause);
        discordController.sendWebhookMessage(jsonPayload);

        // Update player state, weather, and start death animation
        playerController.setPlayerAsDead(player);
        weatherController.updateStormOnPlayerDeath();
        animationController.startDeathAnimation(player);
    }
}
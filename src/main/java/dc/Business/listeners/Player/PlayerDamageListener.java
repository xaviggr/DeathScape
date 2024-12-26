package dc.Business.listeners.Player;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A listener for monitoring and tracking player damage events.
 * Tracks the last time a player received damage and provides utility methods
 * to check if a player has received recent damage.
 */
public class PlayerDamageListener implements Listener {

    /**
     * A thread-safe map to store the timestamp of the last damage received by each player.
     */
    private static final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();

    /**
     * The time threshold (in milliseconds) to consider a player as having received recent damage.
     */
    private static final long DAMAGE_TIME_THRESHOLD = 6000; // 6 seconds in milliseconds

    /**
     * The interval (in milliseconds) to clean up old entries from the damage map.
     */
    private static final long CLEANUP_INTERVAL = 12000; // 12 seconds in milliseconds

    /**
     * Constructs a `PlayerDamageListener` and schedules a periodic cleanup task
     * to remove old damage entries that exceed the threshold.
     *
     * @param plugin The instance of the DeathScape plugin.
     */
    public PlayerDamageListener(DeathScape plugin) {
        // Schedule a cleanup task to run periodically
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupOldEntries, CLEANUP_INTERVAL / 50, CLEANUP_INTERVAL / 50);
    }

    /**
     * Event handler for `EntityDamageEvent`. Tracks the timestamp of the damage event
     * for players.
     *
     * @param event The damage event triggered when an entity takes damage.
     */
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            // Record the current time as the last damage time for the player
            lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Checks if a player has received damage within the defined time threshold.
     *
     * @param player The player to check.
     * @return True if the player has received damage within the threshold, false otherwise.
     */
    public static boolean hasReceivedRecentDamage(Player player) {
        Long lastDamage = lastDamageTime.get(player.getUniqueId());
        if (lastDamage == null) {
            return false;
        }

        // Check if the damage occurred within the time threshold
        return (System.currentTimeMillis() - lastDamage) <= DAMAGE_TIME_THRESHOLD;
    }

    /**
     * Periodically cleans up old entries from the `lastDamageTime` map.
     * Removes entries where the damage timestamp exceeds the defined threshold.
     */
    private void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();

        // Remove entries where the last damage time exceeds the threshold
        lastDamageTime.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > DAMAGE_TIME_THRESHOLD);
    }
}
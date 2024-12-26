package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Controls animations related to player events in the DeathScape plugin.
 */
public class AnimationController {

    /**
     * Reference to the main DeathScape plugin instance.
     */
    private final DeathScape plugin;

    /**
     * Constructs an AnimationController with a reference to the main plugin.
     *
     * @param plugin The main instance of the DeathScape plugin.
     */
    public AnimationController(DeathScape plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts a death animation sequence for a specified player.
     *
     * <p>This animation includes playing sounds, applying blindness,
     * and displaying a sequence of custom titles to all players in the server.
     * The animation ends with a final sound and clears the titles.</p>
     *
     * @param player The player for whom the death animation is triggered.
     */
    public void startDeathAnimation(Player player) {
        // Play the initial death sound and apply a blindness effect to the player.
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound minecraft:muerte player @a ~ ~ ~ 100 1 1");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect give " + player.getName() + " blindness 12 1");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a times 0 10 0");

        // Create a repetitive task to manage the animation sequence.
        new BukkitRunnable() {
            // Index to track the progress of the animation.
            int index = 0;

            @Override
            public void run() {
                // If the index is within the range of the animation, display a title with a custom character.
                if (index <= 168) {
                    String value = "\\uE" + String.format("%03d", index);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + value + "\"}");
                    index++;
                } else {
                    // Play the final sound, adjust the title display time, and stop the task.
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound minecraft:entity.lightning_bolt.impact player @a ~ ~ ~ 100 1 1");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a times 0 20 10");
                    cancel(); // Stop the animation task when all titles are displayed.
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Schedule the task to run immediately and repeat every tick.
    }
}

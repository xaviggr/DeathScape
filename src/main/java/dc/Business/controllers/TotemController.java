package dc.Business.controllers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * Handles the logic for totem mechanics, including success probability and failure handling,
 * in the DeathScape plugin.
 */
public class TotemController {

    private final JavaPlugin plugin;
    private final Random random;

    /**
     * Constructor to initialize the TotemController with the required plugin instance.
     *
     * @param plugin The main instance of the JavaPlugin.
     */
    public TotemController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    /**
     * Gets the plugin configuration file.
     *
     * @return The {@link FileConfiguration} instance of the plugin.
     */
    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    /**
     * Represents the result of the totem success check, including whether the totem succeeds
     * and the random value generated during the check.
     */
    public static class Result {

        private final boolean success;
        private final int randomValue;

        /**
         * Constructs a Result object with the success state and random value.
         *
         * @param success     Whether the totem succeeded.
         * @param randomValue The random value generated during the success check.
         */
        public Result(boolean success, int randomValue) {
            this.success = success;
            this.randomValue = randomValue;
        }

        /**
         * Checks if the totem operation succeeded.
         *
         * @return True if the totem succeeded, false otherwise.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Gets the random value generated during the success check.
         *
         * @return The random value.
         */
        public int getRandomValue() {
            return randomValue;
        }
    }

    /**
     * Determines if the totem operation will succeed based on a probability value
     * from the configuration file.
     *
     * @return A {@link Result} object containing whether the operation succeeded
     * and the random value generated during the check.
     */
    public Result willTotemSucceed() {
        FileConfiguration config = plugin.getConfig();
        int successProbability = config.getInt("config.totem_success_probability");
        int randomValue = random.nextInt(100) + 1;

        // Return the result along with the random value
        return new Result(randomValue <= successProbability, randomValue);
    }

    /**
     * Handles the failure of a totem operation, broadcasting a message to all players
     * and playing a failure sound.
     *
     * @param player      The player who attempted to use the totem.
     * @param randomValue The random value generated during the success check.
     */
    public void handleTotemFailure(Player player, int randomValue) {
        int successProbability = plugin.getConfig().getInt("config.totem_success_probability");

        // Load failure message from config.yml
        String message = plugin.getConfig().getString("messages.totem_failure");
        if (message != null) {
            message = message.replace("{playerName}", player.getName())
                    .replace("{randomValue}", String.valueOf(randomValue))
                    .replace("{successProbability}", String.valueOf(successProbability));

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        }

        // Play failure sound for all online players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
    }
}

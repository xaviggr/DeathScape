package dc.Business.controllers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class TotemController {

    private final JavaPlugin plugin;
    private final Random random;

    public TotemController(JavaPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    // Clase para devolver el resultado y el valor aleatorio generado
    public static class Result {
        private final boolean success;
        private final int randomValue;

        public Result(boolean success, int randomValue) {
            this.success = success;
            this.randomValue = randomValue;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getRandomValue() {
            return randomValue;
        }
    }

    public Result willTotemSucceed() {
        FileConfiguration config = plugin.getConfig();
        int successProbability = config.getInt("config.totem_success_probability");
        int randomValue = random.nextInt(100) + 1;

        // Devuelve el resultado junto con el valor aleatorio generado
        return new Result(randomValue <= successProbability, randomValue);
    }

    public void handleTotemFailure(Player player, int randomValue) {
        int successProbability = plugin.getConfig().getInt("config.totem_success_probability");

        // Cargar mensaje desde config.yml
        String message = plugin.getConfig().getString("messages.totem_failure");
        message = message.replace("{playerName}", player.getName())
                .replace("{randomValue}", String.valueOf(randomValue))
                .replace("{successProbability}", String.valueOf(successProbability));

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
    }
}

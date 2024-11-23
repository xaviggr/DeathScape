package dc.Business.listeners.Player;

import dc.utils.Message;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

import java.util.HashSet;
import java.util.Set;

public class PlayerSleepEventListener implements Listener {

    private final Set<Player> sleepingPlayers = new HashSet<>(); // Set to track sleeping players

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        sleepingPlayers.add(player); // Add player to sleeping list

        // Check if it's nighttime and the player is the first to sleep
        org.bukkit.World world = player.getWorld();
        if (sleepingPlayers.size() == 1 && (world.getTime() >= 13000 && world.getTime() <= 23000)) {
            Bukkit.getWorlds().forEach(w -> w.setTime(0)); // Set time to day
            Message.enviarMensajeColorido(player, "You have made the day arrive!", ChatColor.GOLD);
        }
    }
}

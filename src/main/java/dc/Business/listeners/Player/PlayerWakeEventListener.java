package dc.Business.listeners.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.entity.Player;

import java.util.Set;

public class PlayerWakeEventListener implements Listener {

    private final Set<Player> sleepingPlayers;

    public PlayerWakeEventListener() {
        this.sleepingPlayers = new java.util.HashSet<>();
    }

    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        sleepingPlayers.remove(player); // Remove player from sleeping list
    }
}

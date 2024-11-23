package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerQuitEventListener implements Listener {

    private final PlayerController playerController;

    public PlayerQuitEventListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Save player time played and coordinates
        PlayerEditDatabase.setPlayerCoords(player);
        playerController.deactivateBanshee(player);
    }
}

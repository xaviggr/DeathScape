package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final PlayerController playerController;

    // Constructor
    public PlayerRespawnListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Coordenadas a las que quieres teletransportar al jugador
        double x = 4104; // Cambia estas coordenadas según tu mundo
        double y = 53;
        double z = -547;

        // Mundo en el que estará el jugador
        String worldName = "world"; // Cambia esto si tu mundo tiene otro nombre

        // Obtener el mundo y crear la ubicación
        Location respawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z);

        // Establecer la ubicación del respawn
        event.setRespawnLocation(respawnLocation);
    }
}

package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.DeathScape;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerQuitEventListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    public PlayerQuitEventListener(DeathScape plugin, PlayerController playerController) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Verificar si el jugador tiene poca vida
        double healthThreshold = 4.0; // Cambia esto según el umbral de vida deseado
        if (player.getHealth() <= healthThreshold && PlayerDamageListener.hasReceivedRecentDamage(player)) {
            // Notificar a los jugadores sobre el baneo
            Bukkit.broadcastMessage(ChatColor.RED + "El jugador " + player.getName() + " ha sido baneado por desconectarse tras recibir daño con poca vida.");

            // Manejar el baneo
            handlePlayerBan(player);
        }

        playerController.removePlayerFromServer(player);
    }

    private void handlePlayerBan(Player player) {
        // Banea al jugador
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(
                player.getName(),
                "Desconexión en combate con poca vida",
                null,
                "console"
        );

        // Expulsar al jugador inmediatamente
        player.kickPlayer("Has sido baneado por desconectarte con poca vida.");
    }
}

package dc.Business.listeners;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.StormController;
import dc.DeathScape;
import dc.Persistence.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.Business.controllers.ServerController;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final StormController stormController;

    public PlayerListener(DeathScape plugin, ServerController serverController, PlayerController playerController, StormController stormController) {
        this.plugin = plugin;
        this.playerController = playerController;
        this.stormController = stormController;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());
        playerController.setPlayerAsDead(player);
        stormController.updateStormOnPlayerDeath();
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostaddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        if (PlayerDatabase.getPlayerDataFromDatabase(player.getName()) == null) {
            PlayerData playerData = new PlayerData(player.getName(), false, 0, hostaddress, "0", player.getUniqueId(), "0", "0", "0");
            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error al cargar tus datos, contacta con un administrador.");
            }
        } else {
            PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
            assert playerData != null;
            if (playerData.isDead() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Estás muerto, contacta con un administrador.");
            }
            if (!Objects.equals(playerData.getHostAddress(), hostaddress) && plugin.getMainConfigManager().isKick_if_ip_changed() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Tu IP ha cambiado, contacta con un administrador.");
            }
        }

        if (player.isOnline()) {
            plugin.time_of_connection.put(player.getName(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (plugin.time_of_connection.containsKey(playerName)) {
            long initTime = plugin.time_of_connection.get(playerName);
            long actualTime = System.currentTimeMillis();
            long onlineTime = actualTime - initTime;

            int segundos = Integer.parseInt(String.valueOf((int) (onlineTime / 1000) % 60));
            int minutos = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60)) % 60)));
            int horas = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60 * 60)) % 24)));
            PlayerEditDatabase.setPlayerTimePlayed(player, segundos, minutos, horas);
            plugin.time_of_connection.remove(playerName);
        }

        PlayerEditDatabase.setPlayerCoords(player);
    }
}

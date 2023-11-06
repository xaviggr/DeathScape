package dc.listeners;

import dc.DeathScape;
import dc.config.PlayerData;
import dc.config.PlayerDatabase;
import dc.config.PlayerEditDatabase;
import dc.player.PlayerDeath;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final DeathScape plugin;

    public PlayerListener(DeathScape plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        //Futura implementación prohibir palabras prohibidas.
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerDeath.Dead(Objects.requireNonNull(event.getEntity().getPlayer()), plugin);
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
            plugin.tiempoDeConexion.put(player.getName(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String nombreJugador = player.getName();

        if (plugin.tiempoDeConexion.containsKey(nombreJugador)) {
            long tiempoDeInicio = plugin.tiempoDeConexion.get(nombreJugador);
            long tiempoActual = System.currentTimeMillis();
            long tiempoEnLinea = tiempoActual - tiempoDeInicio;

            int segundos = Integer.parseInt(String.valueOf((int) (tiempoEnLinea / 1000) % 60));
            int minutos = Integer.parseInt(String.valueOf((int) ((tiempoEnLinea / (1000 * 60)) % 60)));
            int horas = Integer.parseInt(String.valueOf((int) ((tiempoEnLinea / (1000 * 60 * 60)) % 24)));
            PlayerEditDatabase.setPlayerTimePlayed(player, segundos, minutos, horas);
            plugin.tiempoDeConexion.remove(nombreJugador);
        }

        PlayerEditDatabase.setPlayerCoords(player);
    }
}

package dc.listeners;

import dc.DeathScape;
import dc.config.PlayerData;
import dc.config.PlayerDatabase;
import dc.player.PlayerDeath;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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
        PlayerDeath.Dead (Objects.requireNonNull (event.getEntity ().getPlayer ()), plugin);
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        //if(player.)
        String hostaddress = Objects.requireNonNull (player.getAddress ()).getAddress().getHostAddress ();
        PlayerData playerData = new PlayerData (player.getName (), false,0, hostaddress, "0" ,player.getUniqueId());
        if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
            player.kickPlayer (ChatColor.RED + "Error al cargar tus datos, contacta con un administrador.");
        }
    }
}

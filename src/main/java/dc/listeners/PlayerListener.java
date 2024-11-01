package dc.listeners;

import dc.DeathScape;
import dc.player.PlayerDeath;
import dc.server.ServerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;

public class PlayerListener implements Listener {

    private final DeathScape plugin;
    private final ServerData serverData;

    public PlayerListener(DeathScape plugin, ServerData serverData) {
        this.plugin = plugin;
        this.serverData = serverData;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());

        // Llama al método Dead para manejar la muerte del jugador
        PlayerDeath.Dead(player, plugin);

        // Agrega 5 minutos al contador de lluvia
        serverData.agregarTiempoLluvia(5);

        // Inicia la lluvia si no está activa
        if (!Bukkit.getWorlds().get(0).hasStorm()) {
            Bukkit.getWorlds().get(0).setStorm(true);
        }

        // Programa una tarea que reducirá el tiempo de lluvia pendiente cada minuto
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (serverData.getTiempoLluviaPendiente() > 0) {
                // Reduce el tiempo de lluvia en 1 minuto
                serverData.reducirTiempoLluvia(1);
            } else {
                // Si no hay más tiempo de lluvia pendiente, detiene la lluvia
                Bukkit.getWorlds().get(0).setStorm(false);
            }
        }, 0L, 20L * 60); // Ejecuta cada minuto (20 ticks = 1 segundo)
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // Futura implementación para prohibir palabras.
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        // Código de gestión de conexión y datos del jugador.
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Código para calcular el tiempo de conexión del jugador.
    }
}

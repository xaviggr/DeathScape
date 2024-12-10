package dc.Business.listeners.Player;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDamageListener implements Listener {

    private static final Map<UUID, Long> lastDamageTime = new ConcurrentHashMap<>();
    private static final long DAMAGE_TIME_THRESHOLD = 6000; // 6 segundos en milisegundos
    private static final long CLEANUP_INTERVAL = 12000; // 12 segundos en milisegundos

    public PlayerDamageListener(DeathScape plugin) {
        // Programar una tarea para limpiar las entradas obsoletas
        Bukkit.getScheduler().runTaskTimer(plugin, this::cleanupOldEntries, CLEANUP_INTERVAL / 50, CLEANUP_INTERVAL / 50);
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public static boolean hasReceivedRecentDamage(Player player) {
        Long lastDamage = lastDamageTime.get(player.getUniqueId());
        if (lastDamage == null) {
            return false;
        }

        // Verificar si el daño ocurrió dentro del umbral de tiempo
        return (System.currentTimeMillis() - lastDamage) <= DAMAGE_TIME_THRESHOLD;
    }

    private void cleanupOldEntries() {
        long currentTime = System.currentTimeMillis();

        lastDamageTime.entrySet().removeIf(entry -> (currentTime - entry.getValue()) > DAMAGE_TIME_THRESHOLD);
    }
}
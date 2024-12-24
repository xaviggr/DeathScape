package dc.Business.listeners.Player;

import dc.Business.controllers.ServerController;
import dc.Business.controllers.WeatherController;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerSleepEventListener implements Listener {

    private final WeatherController weatherController;

    // Constructor
    public PlayerSleepEventListener(WeatherController weatherController) {
        this.weatherController = weatherController;
    }

    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Cancelar el evento si es de noche
        if (!weatherController.isNightTime()) {
            event.setCancelled(true);
            Message.enviarMensajeColorido(player, "No puedes dormir durante el día.", ChatColor.RED);
            return;
        }

        // Cancelar el evento si intenta dormir durante el dia con tormenta
        if (world.hasStorm() && !weatherController.isNightTime()) {
            event.setCancelled(true);
            Message.enviarMensajeColorido(player, "No puedes dormir durante la tormenta.", ChatColor.RED);
            return;
        }

        // Agregar jugador a la lista de durmiendo
        ServerController.SleepingPlayers.add(player);

        // Si es el primer jugador durmiendo, cambia el tiempo a día
        if (ServerController.SleepingPlayers.size() == 1) {
            if (weatherController.isNightTime()) { // Verificar si es de noche nuevamente por seguridad
                world.setTime(0); // Cambiar el tiempo a día
                Message.sendMessageAllPlayers("El jugador " + player.getName() + " ha hecho que amanezca.", ChatColor.GOLD);
            }

            // Asegúrate de que la lluvia no se detenga
            if (world.hasStorm()) {
                world.setStorm(true); // Mantén la lluvia si ya estaba activa
            }
        }
    }

    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        ServerController.SleepingPlayers.remove(player); // Eliminar jugador de la lista de durmiendo
    }
}
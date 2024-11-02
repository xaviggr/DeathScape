package dc.Business.controllers;

import dc.DeathScape;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AnimationController {

    private final DeathScape plugin;

    public AnimationController(DeathScape plugin) {
        this.plugin = plugin;
    }

    public void startDeathAnimation(Player player) {
        // Ejecutar el sonido inicial y aplicar el efecto de ceguera
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound minecraft:muerte player @a ~ ~ ~ 100 1 1");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect give " + player.getName() + " blindness 12 1");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a times 0 10 0");
        // Crear una tarea repetitiva para la animación
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                // Si el índice está dentro del rango, envía el título
                if (index <= 168) {
                    String value = "\\uE" + String.format("%03d", index);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a title {\"text\":\"" + value + "\"}");
                    index++;
                } else {
                    // Reproducir el sonido final y cancelar la tarea
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound minecraft:entity.lightning_bolt.impact player @a ~ ~ ~ 100 1 1");
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "title @a times 0 20 10");
                    cancel(); // Detener la tarea una vez que se envían todos los títulos
                }
            }
        }.runTaskTimer(plugin, 0, 1); // Ejecutar inmediatamente y repetir cada 2 ticks (ajusta el intervalo según sea necesario)
    }
}

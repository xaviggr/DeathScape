package dc.Business.controllers;

import dc.DeathScape;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class ServerController {
    private final File configFile;
    private final FileConfiguration config;
    private boolean rainTaskActive;
    private final DeathScape plugin;

    private int playerCounter = 0; // Contador de jugadores
    private final Queue<Player> queue = new LinkedList<>(); // Cola de jugadores

    public ServerController(DeathScape plugin) {
        this.plugin = plugin;
        // Carga el archivo de configuración
        configFile = new File(plugin.getDataFolder(), "serverData.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Inicialización de valores predeterminados
        if (!config.isSet("last_update")) {
            config.set("last_update", Instant.now().toEpochMilli());
        }
        if (!config.isSet("storm_time_pending")) {
            config.set("storm_time_pending", 0);
        }
        saveConfig();

        rainTaskActive = false;
        checkDay();
        startDayUpdater();
    }

    private void startDayUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkDay();
            Bukkit.getConsoleSender().sendMessage("Días del servidor actualizados automáticamente.");
        }, 0L, 24 * 60 * 60 * 20L);
    }

    public void checkDay() {
        // Calcula los días transcurridos y actualiza el contador
        Instant ultimaActualizacion = Instant.ofEpochMilli(config.getLong("last_update"));
        Instant ahora = Instant.now();
        long diasTranscurridos = Duration.between(ultimaActualizacion, ahora).toDays();

        config.set("server_days", config.getInt("server_days", 0) + diasTranscurridos);
        config.set("last_update", Instant.now().toEpochMilli());
        saveConfig();
    }

    public int getServerDays() {
        return config.getInt("server_days");
    }

    public void setServerDays(int dia) {
        config.set("server_days", dia);
        saveConfig();
    }

    // Métodos para gestionar el tiempo de lluvia pendiente
    public int getStormPendingTime() {
        return config.getInt("storm_time_pending");
    }

    public void addStormTime(int minutos) {
        int tiempoActual = config.getInt("storm_time_pending");
        config.set("storm_time_pending", tiempoActual + minutos);
        saveConfig();
    }

    public void reduceStormTime(int minutos) {
        int tiempoActual = config.getInt("storm_time_pending");
        int nuevoTiempo = Math.max(0, tiempoActual - minutos);
        config.set("storm_time_pending", nuevoTiempo);
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRainTaskActive() {
        return rainTaskActive;
    }

    public void setRainTaskActive(boolean isActive) {
        this.rainTaskActive = isActive;
    }

    public void checkQueueStatus() {
        System.out.println("Jugadores en cola: " + queue.size());
        // Comprueba si hay jugadores en la cola
        // Máximo de jugadores permitidos en el Overworld
        int maxPlayersInWorld = 1;
        if (!queue.isEmpty() && playerCounter < maxPlayersInWorld) {
            Player player = queue.poll();
            Location location = PlayerEditDatabase.getPlayerLocation(Objects.requireNonNull(player).getName());
            if (location == null) {
                player.kickPlayer("Error al cargar tu ubicación, por favor contacta a un administrador.");
                return;
            }
            player.teleport(location);
            increasePlayerCounter();
        }
    }

    public void increasePlayerCounter() {
        playerCounter++;
    }

    public void decreasePlayerCounter() {
        playerCounter--;
    }

    public int getPlayerCounter() {
        return playerCounter;
    }

    public void addPlayerToQueue(Player player) {
        queue.add(player);
        checkQueueStatus();
        System.out.println("Jugador " + player.getName() + " añadido a la cola.");
        printQueu();
    }

    public void removePlayerFromQueue(Player player) {
        queue.remove(player);
        checkQueueStatus();
        System.out.println("Jugador " + player.getName() + " eliminado de la cola.");
        printQueu();
    }

    private void printQueu() {
        System.out.println("Cola de jugadores:");
        for (Player player : queue) {
            System.out.println(player.getName());
        }

        System.out.println("Jugadores en el servidor:" + playerCounter);
    }

    public int getQueuePosition(Player player) {
        return new ArrayList<>(queue).indexOf(player) + 1;
    }
}


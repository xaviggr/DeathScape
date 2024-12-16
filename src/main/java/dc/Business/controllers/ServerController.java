package dc.Business.controllers;

import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
        startPromotionMessages();
    }

    private void startDayUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkDay();
            Bukkit.getConsoleSender().sendMessage("Días del servidor actualizados automáticamente.");
        }, 0L, 24 * 60 * 60 * 20L); // 24 horas
    }

    //Coroutine Mensajes cada x tiempo por el chat para promocionar la tienda y eventos del servidor.
    private void startPromotionMessages() {
        // Lista de mensajes promocionales
        List<String> messages = new ArrayList<>();
        messages.add(ChatColor.GOLD + "[Tienda]: " + ChatColor.YELLOW + "¡Visita nuestra tienda y mejora tu experiencia! " + ChatColor.AQUA + "Usa /tienda para más información.");
        messages.add(ChatColor.GREEN + "[Eventos]: " + ChatColor.LIGHT_PURPLE + "¡No te pierdas los eventos semanales! " + ChatColor.WHITE + "Consulta el calendario con " + ChatColor.AQUA + "/eventos.");
        messages.add(ChatColor.BLUE + "[Tienda]: " + ChatColor.YELLOW + "¡Kits exclusivos disponibles ahora! " + ChatColor.AQUA + "¡Corre a la tienda y échales un vistazo! /tienda");
        messages.add(ChatColor.RED + "[Eventos]: " + ChatColor.GOLD + "¡Participa en los torneos y gana grandes premios! " + ChatColor.WHITE + "Regístrate ahora con " + ChatColor.AQUA + "/torneos.");
        messages.add(ChatColor.DARK_GREEN + "[Tienda]: " + ChatColor.YELLOW + "¡Ofertas especiales este mes! No pierdas la oportunidad de conseguir tus items favoritos. /tienda");

        // Índice de mensajes
        int[] currentIndex = {0};

        // Iniciar el scheduler
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            // Enviar el mensaje actual a todos los jugadores
            String message = messages.get(currentIndex[0]);
            Bukkit.broadcastMessage(message);

            // Actualizar el índice al siguiente mensaje
            currentIndex[0] = (currentIndex[0] + 1) % messages.size();
        }, 0L, 20 * 60 * 20L); // 20 minutos
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
        // Comprueba si hay jugadores en la cola
        // Máximo de jugadores permitidos en el Overworld
        int maxPlayersInWorld = MainConfigManager.getInstance().getMaxPlayersInWorld();
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
    }

    public void removePlayerFromQueue(Player player) {
        queue.remove(player);
        checkQueueStatus();
    }

    public int getQueuePosition(Player player) {
        return new ArrayList<>(queue).indexOf(player) + 1;
    }
}


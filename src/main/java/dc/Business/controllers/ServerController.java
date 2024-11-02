package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class ServerController {
    private final File configFile;
    private final FileConfiguration config;
    private boolean rainTaskActive;

    public ServerController(DeathScape plugin) {
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
}


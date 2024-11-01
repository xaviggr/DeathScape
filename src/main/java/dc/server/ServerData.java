package dc.server;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class ServerData {
    private final File configFile;
    private final FileConfiguration config;

    public ServerData(DeathScape plugin) {
        // Carga el archivo de configuración
        configFile = new File(plugin.getDataFolder(), "dias_servidor.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Inicialización de valores predeterminados
        if (!config.isSet("ultima_actualizacion")) {
            config.set("ultima_actualizacion", Instant.now().toEpochMilli());
        }
        if (!config.isSet("tiempo_lluvia_pendiente")) {
            config.set("tiempo_lluvia_pendiente", 0);
        }
        saveConfig();

        checkDay();
    }

    public void checkDay() {
        // Calcula los días transcurridos y actualiza el contador
        Instant ultimaActualizacion = Instant.ofEpochMilli(config.getLong("ultima_actualizacion"));
        Instant ahora = Instant.now();
        long diasTranscurridos = Duration.between(ultimaActualizacion, ahora).toDays();

        config.set("dias_servidor", config.getInt("dias_servidor", 0) + diasTranscurridos);
        config.set("ultima_actualizacion", Instant.now().toEpochMilli());
        saveConfig();
    }

    public int getServerDays() {
        return config.getInt("dias_servidor");
    }

    public void setServerDays(int dia) {
        config.set("dias_servidor", dia);
        saveConfig();
    }

    // Métodos para gestionar el tiempo de lluvia pendiente
    public int getTiempoLluviaPendiente() {
        return config.getInt("tiempo_lluvia_pendiente");
    }

    public void agregarTiempoLluvia(int minutos) {
        int tiempoActual = config.getInt("tiempo_lluvia_pendiente");
        config.set("tiempo_lluvia_pendiente", tiempoActual + minutos);
        saveConfig();
        Bukkit.getLogger().info("Tiempo de lluvia aumentado en " + minutos + " minutos. Total pendiente: " + (tiempoActual + minutos) + " minutos.");
    }

    public void reducirTiempoLluvia(int minutos) {
        int tiempoActual = config.getInt("tiempo_lluvia_pendiente");
        int nuevoTiempo = Math.max(0, tiempoActual - minutos);
        config.set("tiempo_lluvia_pendiente", nuevoTiempo);
        saveConfig();
        Bukkit.getLogger().info("Tiempo de lluvia reducido en " + minutos + " minutos. Total pendiente: " + nuevoTiempo + " minutos.");
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


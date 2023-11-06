package dc.server;

import dc.DeathScape;
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

        // Verifica si el archivo existe
        if (!config.isSet("ultima_actualizacion")) {
            config.set("ultima_actualizacion", Instant.now().toEpochMilli());
            saveConfig();
        }

        checkDay();
    }

    public void checkDay(){
        // Obtén la última actualización y calcula los días transcurridos
        Instant ultimaActualizacion = Instant.ofEpochMilli(config.getLong("ultima_actualizacion"));
        Instant ahora = Instant.now();
        long diasTranscurridos = Duration.between(ultimaActualizacion, ahora).toDays();

        config.set("dias_servidor", config.getInt("dias_servidor", 0) + diasTranscurridos);
        config.set("ultima_actualizacion", Instant.now().toEpochMilli());
        saveConfig();
    }

    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getServerDays() {
        return config.getInt("dias_servidor");
    }

    public void setServerDays(int dia) {
        config.set("dias_servidor", dia);
        saveConfig();
    }
}

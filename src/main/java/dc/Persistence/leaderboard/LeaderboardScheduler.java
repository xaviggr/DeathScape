package dc.Persistence.leaderboard;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class LeaderboardScheduler {

    private static final String OUTPUT_PATH = "plugins/DeathScape/index.html";

    public static void start(JavaPlugin plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    // 1. Generar el HTML
                    LeaderboardExporter.exportLeaderboardAsHtml(OUTPUT_PATH);

                    // 2. Subir a GitHub automáticamente
                    GitHubUploader.uploadFile(OUTPUT_PATH);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 20L * 10 * 60); // cada 10 minutos
    }
}

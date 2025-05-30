package dc.Persistence.leaderboard;

import dc.Persistence.config.MainConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;

public class LeaderboardScheduler {

    private static final String OUTPUT_PATH = "plugins/DeathScape/index.html";

    public static void start(JavaPlugin plugin) {
        int intervalMinutes = MainConfigManager.getInstance().getLeaderboardUpdateInterval();
        long intervalTicks = 20L * 60 * intervalMinutes;

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
        }.runTaskTimerAsynchronously(plugin, 0L, intervalTicks); // cada X minutos según config
    }
}


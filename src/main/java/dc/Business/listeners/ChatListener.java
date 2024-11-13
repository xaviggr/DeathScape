package dc.Business.listeners;

import dc.Business.inventory.ReportInventory;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import java.util.Map;

public class ChatListener implements Listener {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Maneja los comentarios de reporte, si el jugador está en proceso de reportar
        if (isPendingReport(player)) {
            handlePlayerReport(event, player, message);
            return; // Salimos, ya que el mensaje fue tratado como un reporte
        }

        // Verifica y bloquea palabras prohibidas en el mensaje
        if (containsBannedWords(message)) {
            handleBannedWords(event, player, message);
        }
    }

    // Verifica si el jugador está en proceso de reportar
    private boolean isPendingReport(Player player) {
        return ReportInventory.getPendingReports().containsKey(player);
    }

    // Maneja el comentario de reporte y lo envía
    private void handlePlayerReport(AsyncPlayerChatEvent event, Player player, String comment) {
        event.setCancelled(true);  // Cancela el mensaje en el chat global

        Map<Player, String> pendingReports = ReportInventory.getPendingReports();
        String reportedPlayerName = pendingReports.get(player);

        sendReport(player, reportedPlayerName, comment);
        Message.enviarMensajeColorido(player, "Gracias por tu reporte.", ChatColor.GREEN);
        pendingReports.remove(player);
    }

    // Envía el reporte a administradores o lo guarda en la base de datos
    private void sendReport(Player reporter, String reportedPlayerName, String comment) {
        ReportsDatabase.addReport(reporter.getName(), reportedPlayerName, comment);
    }

    // Verifica si el mensaje contiene palabras prohibidas
    private boolean containsBannedWords(String message) {
        for (String bannedWord : BannedWordsDatabase.getBannedWords()) {
            if (message.toLowerCase().contains(bannedWord)) {
                return true;
            }
        }
        return false;
    }

    // Maneja el caso de palabras prohibidas en el mensaje
    private void handleBannedWords(AsyncPlayerChatEvent event, Player player, String message) {
        event.setCancelled(true);
        Message.enviarMensajeColorido(player, "Por favor, no uses palabras prohibidas.", ChatColor.RED);
    }
}

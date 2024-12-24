package dc.Business.listeners;

import dc.Business.controllers.PlayerController;
import dc.Business.inventory.ReportInventory;
import dc.DeathScape;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.chat.ReportsDatabase;
import dc.Persistence.groups.GroupDatabase;
import dc.utils.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ChatListener implements Listener {

    private static final int SPAM_TIME_LIMIT = 2000; // Tiempo límite entre mensajes en milisegundos (2 segundos)
    private static final int MESSAGE_HISTORY_LIMIT = 2; // Número máximo de mensajes en el historial
    private static final int MESSAGE_EXPIRATION_TIME = 15000; // Tiempo para limpiar mensajes antiguos (15 segundos)

    private final PlayerController playerController;
    private final DeathScape plugin;

    // Mapas para rastrear mensajes, timestamps y tiempo del último mensaje
    private final Map<Player, List<String>> playerMessages = new HashMap<>();
    private final Map<Player, List<Long>> messageTimestamps = new HashMap<>();
    private final Map<Player, Long> lastMessageTimes = new HashMap<>();

    public ChatListener(PlayerController playerController, DeathScape plugin) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    /**
     * Maneja el evento de chat de los jugadores.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Verificar si el jugador está spameando
        if (isSpamming(player)) {
            cancelChat(event, player, "Estás escribiendo demasiado rápido. Por favor, reduce la velocidad.");
            return;
        }

        // Verificar si el mensaje es repetido
        if (isRepeatedMessage(player, message)) {
            cancelChat(event, player, "No puedes enviar el mismo mensaje repetidamente.");
            return;
        }

        // Guardar el mensaje y su timestamp
        saveMessage(player, message);

        // Limpiar mensajes antiguos
        startCleanupTask(player);

        // Verificar si el jugador está reportando
        if (isPendingReport(player)) {
            handlePlayerReport(event, player, message);
            return;
        }

        // Verificar si el mensaje contiene palabras prohibidas
        if (containsBannedWords(message)) {
            handleBannedWords(event, player);
            return;
        }

        // Verificar si el jugador está silenciado
        if (playerController.isMuted(player)) {
            cancelChat(event, player, "Estás silenciado. No puedes enviar mensajes.");
            return;
        }

        // Añadir prefijo del grupo y formatear el mensaje
        formatAndSendMessage(event, player, message);
    }

    /**
     * Cancela el mensaje de chat y envía un mensaje al jugador.
     */
    private void cancelChat(AsyncPlayerChatEvent event, Player player, String reason) {
        event.setCancelled(true);
        Message.enviarMensajeColorido(player, reason, ChatColor.RED);
    }

    /**
     * Guarda el mensaje del jugador y su timestamp.
     */
    private void saveMessage(Player player, String message) {
        playerMessages.computeIfAbsent(player, k -> new ArrayList<>()).add(message);
        messageTimestamps.computeIfAbsent(player, k -> new ArrayList<>()).add(System.currentTimeMillis());

        // Limitar el historial a los últimos MESSAGE_HISTORY_LIMIT mensajes
        List<String> messages = playerMessages.get(player);
        List<Long> timestamps = messageTimestamps.get(player);
        while (messages.size() > MESSAGE_HISTORY_LIMIT) {
            messages.remove(0);
            timestamps.remove(0);
        }
    }

    /**
     * Limpia mensajes antiguos de un jugador.
     */
    private void startCleanupTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Long> timestamps = messageTimestamps.get(player);
                List<String> messages = playerMessages.get(player);
                if (timestamps == null || messages == null) return;

                long now = System.currentTimeMillis();
                timestamps.removeIf(timestamp -> now - timestamp > MESSAGE_EXPIRATION_TIME);
                messages.removeIf(msg -> timestamps.isEmpty());
            }
        }.runTaskLater(plugin, 20L); // Limpiar después de 1 segundo
    }

    /**
     * Verifica si el jugador está spameando.
     */
    private boolean isSpamming(Player player) {
        long now = System.currentTimeMillis();
        long lastMessageTime = lastMessageTimes.getOrDefault(player, 0L);

        if (now - lastMessageTime < SPAM_TIME_LIMIT) {
            return true; // Considerar como spam
        }

        lastMessageTimes.put(player, now);
        return false;
    }

    /**
     * Verifica si el mensaje es repetido.
     */
    private boolean isRepeatedMessage(Player player, String message) {
        List<String> recentMessages = playerMessages.getOrDefault(player, Collections.emptyList());
        return recentMessages.stream().anyMatch(msg -> msg.equalsIgnoreCase(message));
    }

    /**
     * Verifica si el jugador está haciendo un reporte.
     */
    private boolean isPendingReport(Player player) {
        return ReportInventory.getPendingReports().containsKey(player);
    }

    /**
     * Maneja el mensaje si el jugador está reportando.
     */
    private void handlePlayerReport(AsyncPlayerChatEvent event, Player player, String comment) {
        event.setCancelled(true);
        String reportedPlayer = ReportInventory.getPendingReports().get(player);
        sendReport(player, reportedPlayer, comment);
        ReportInventory.getPendingReports().remove(player);
        Message.enviarMensajeColorido(player, "Gracias por tu reporte.", ChatColor.GREEN);
    }

    /**
     * Envía un reporte al sistema de base de datos.
     */
    private void sendReport(Player reporter, String reportedPlayer, String comment) {
        ReportsDatabase.addReport(reporter.getName(), reportedPlayer, comment);
    }

    /**
     * Verifica si el mensaje contiene palabras prohibidas.
     */
    private boolean containsBannedWords(String message) {
        return BannedWordsDatabase.getBannedWords().stream()
                .anyMatch(bannedWord -> message.toLowerCase().contains(bannedWord.toLowerCase()));
    }

    /**
     * Maneja un mensaje con palabras prohibidas.
     */
    private void handleBannedWords(AsyncPlayerChatEvent event, Player player) {
        cancelChat(event, player, "Por favor, no uses palabras prohibidas.");
    }

    /**
     * Formatea el mensaje con el prefijo del grupo y lo envía.
     */
    private void formatAndSendMessage(AsyncPlayerChatEvent event, Player player, String message) {
        // Escapar cualquier "%" en el mensaje del jugador
        message = message.replace("%", "%%");

        // Obtener el prefijo del grupo
        String group = playerController.getGroupFromPlayer(player);
        String prefix = ChatColor.translateAlternateColorCodes(
                '&',
                Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(group))
        );

        // Configurar el prefijo en negrita
        String boldPrefix = ChatColor.BOLD + prefix;

        // Formatear el mensaje con colores
        String formattedMessage = String.format(
                "%s%s %s%s: %s",
                boldPrefix, // Prefijo con color y negrita
                ChatColor.RESET, // Reset para limpiar colores después del prefijo
                ChatColor.WHITE, // Color blanco para el nombre del jugador
                player.getDisplayName(),
                message // El mensaje del jugador
        );

        // Configurar el formato del evento de chat
        event.setFormat(formattedMessage);
    }
}

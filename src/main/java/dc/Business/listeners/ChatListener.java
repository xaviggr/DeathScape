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

    private final PlayerController playerController;
    private final DeathScape plugin;

    // Almacena una lista de los últimos mensajes por jugador
    private final Map<Player, List<String>> playerMessages = new HashMap<>();
    private final Map<Player, List<Long>> messageTimestamps = new HashMap<>();
    private final Map<Player, Long> lastMessageTimes = new HashMap<>(); // Almacena el timestamp del último mensaje enviado

    public ChatListener(PlayerController playerController, DeathScape plugin) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    /**
     * Handles player chat events. Checks for repeated messages, spam rate, banned words, and processes reports.
     *
     * @param event The AsyncPlayerChatEvent triggered when a player sends a message.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // Verificar si el jugador está enviando mensajes demasiado rápido
        if (isSpamming(player)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Estás escribiendo demasiado rápido. Por favor, reduce la velocidad.");
            return;
        }

        // Inicializar listas si el jugador no tiene historial
        playerMessages.putIfAbsent(player, new ArrayList<>());
        messageTimestamps.putIfAbsent(player, new ArrayList<>());

        // Verificar si el mensaje ya existe en los últimos enviados
        if (isRepeatedMessage(player, message)) {
            event.setCancelled(true); // Cancelar el mensaje repetido
            player.sendMessage(ChatColor.RED + "No puedes enviar el mismo mensaje repetidamente.");
            return;
        }

        // Guardar el mensaje y su timestamp actual
        saveMessage(player, message);

        // Iniciar la rutina para limpiar mensajes antiguos
        startCleanupTask(player);

        // Check if the player is in the process of making a report
        if (isPendingReport(player)) {
            handlePlayerReport(event, player, message);
            return; // Exit after handling the report
        }

        // Check for banned words in the message
        if (containsBannedWords(message)) {
            handleBannedWords(event, player);
            return; // Exit after handling the banned words
        }

        if (playerController.isMuted(player)) {
            event.setCancelled(true); // Cancel the chat message
            Message.enviarMensajeColorido(player, "Estás silenciado. No puedes enviar mensajes.", ChatColor.RED);
            return; // Exit after handling the mute
        }

        // Add group prefix and color to the message
        String group = playerController.getGroupFromPlayer(player);
        String prefix = ChatColor.translateAlternateColorCodes(
                '&',
                Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(group))
        );

        // Format: Prefix only colored, message follows in default color
        String formattedMessage = String.format(
                "%s%s %s%s: %s",
                prefix,
                ChatColor.RESET, // Reset colors after the prefix
                ChatColor.WHITE, // Optional: Add white color to name for clarity
                player.getDisplayName(),
                message
        );

        event.setFormat(formattedMessage);
    }

    /**
     * Verifica si el jugador está enviando mensajes demasiado rápido.
     *
     * @param player El jugador que envía el mensaje.
     * @return Verdadero si el jugador está enviando mensajes demasiado rápido, falso en caso contrario.
     */
    private boolean isSpamming(Player player) {
        long now = System.currentTimeMillis();
        long lastMessageTime = lastMessageTimes.getOrDefault(player, 0L);

        // Si el jugador envía un mensaje en menos de 2 segundos, se considera spam
        if (now - lastMessageTime < 2000) { // 2000 ms = 2 segundos
            return true;
        }

        // Actualizar el tiempo del último mensaje
        lastMessageTimes.put(player, now);
        return false;
    }

    /**
     * Verifica si el mensaje enviado ya existe en los últimos mensajes del jugador.
     *
     * @param player  El jugador que envía el mensaje.
     * @param message El mensaje enviado.
     * @return Verdadero si el mensaje ya existe, falso en caso contrario.
     */
    private boolean isRepeatedMessage(Player player, String message) {
        List<String> recentMessages = playerMessages.get(player);
        return recentMessages.stream().anyMatch(msg -> msg.equalsIgnoreCase(message));
    }

    /**
     * Guarda el mensaje del jugador y elimina los mensajes antigustopos si exceden el límite de tiempo o cantidad.
     *
     * @param player  El jugador que envía el mensaje.
     * @param message El mensaje enviado.
     */
    private void saveMessage(Player player, String message) {
        List<String> recentMessages = playerMessages.get(player);
        List<Long> timestamps = messageTimestamps.get(player);

        // Agregar el mensaje y el timestamp
        recentMessages.add(message);
        timestamps.add(System.currentTimeMillis());

        // Mantener solo los últimos 2 mensajes en la lista
        if (recentMessages.size() > 2) {
            recentMessages.remove(0);
            timestamps.remove(0);
        }
    }

    /**
     * Inicia una tarea para limpiar mensajes antiguos de un jugador.
     *
     * @param player El jugador cuyos mensajes serán limpiados.
     */
    private void startCleanupTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                List<Long> timestamps = messageTimestamps.get(player);
                List<String> recentMessages = playerMessages.get(player);

                if (timestamps == null || recentMessages == null) return;

                long now = System.currentTimeMillis();
                Iterator<Long> timestampIterator = timestamps.iterator();
                Iterator<String> messageIterator = recentMessages.iterator();

                // Eliminar mensajes que tengan más de 15 segundos
                while (timestampIterator.hasNext() && messageIterator.hasNext()) {
                    Long timestamp = timestampIterator.next();
                    messageIterator.next();

                    if (now - timestamp > 15000) {
                        timestampIterator.remove();
                        messageIterator.remove();
                    }
                }
            }
        }.runTaskLater(plugin, 15 * 20L); // 15 segundos = 15 * 20 ticks
    }

    /**
     * Checks if the player is currently in the process of reporting another player.
     *
     * @param player The player sending the chat message.
     * @return True if the player is reporting, otherwise false.
     */
    private boolean isPendingReport(Player player) {
        return ReportInventory.getPendingReports().containsKey(player);
    }

    /**
     * Handles the report comment from the player and sends it to the database.
     *
     * @param event   The chat event.
     * @param player  The player making the report.
     * @param comment The comment from the player about the report.
     */
    private void handlePlayerReport(AsyncPlayerChatEvent event, Player player, String comment) {
        event.setCancelled(true); // Cancel the chat message to prevent it from being sent to others

        Map<Player, String> pendingReports = ReportInventory.getPendingReports();
        String reportedPlayerName = pendingReports.get(player); // Get the name of the player being reported

        sendReport(player, reportedPlayerName, comment); // Send the report
        Message.enviarMensajeColorido(player, "Gracias por tu reporte.", ChatColor.GREEN); // Acknowledge the report
        pendingReports.remove(player); // Remove the player from the pending reports list
    }

    /**
     * Sends the report to the database for processing.
     *
     * @param reporter          The player making the report.
     * @param reportedPlayerName The name of the player being reported.
     * @param comment           The comment or reason for the report.
     */
    private void sendReport(Player reporter, String reportedPlayerName, String comment) {
        ReportsDatabase.addReport(reporter.getName(), reportedPlayerName, comment); // Save the report to the database
    }

    /**
     * Checks if the message contains any banned words.
     *
     * @param message The message to check.
     * @return True if the message contains a banned word, otherwise false.
     */
    private boolean containsBannedWords(String message) {
        for (String bannedWord : BannedWordsDatabase.getBannedWords()) {
            if (message.toLowerCase().contains(bannedWord)) { // Case-insensitive check for banned words
                return true;
            }
        }
        return false; // No banned words found
    }

    /**
     * Handles a message containing banned words by cancelling the event and notifying the player.
     *
     * @param event  The chat event.
     * @param player The player who sent the message.
     */
    private void handleBannedWords(AsyncPlayerChatEvent event, Player player) {
        event.setCancelled(true); // Cancel the chat message
        Message.enviarMensajeColorido(player, "Por favor, no uses palabras prohibidas.", ChatColor.RED); // Notify the player
    }
}
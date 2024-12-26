package dc.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class to handle sending messages to players or all online players with color formatting.
 */
public class Message {

    /**
     * Sends a formatted and colorful message to a specific player.
     *
     * @param jugador The player to whom the message will be sent.
     * @param mensaje The message to send.
     * @param color   The color of the message.
     */
    public static void enviarMensajeColorido(Player jugador, String mensaje, ChatColor color) {
        jugador.sendMessage(color + mensaje);
    }

    /**
     * Sends a confirmation message to a player indicating the configuration has been successfully reloaded.
     *
     * @param jugador The player to whom the message will be sent.
     */
    public static void ConfigLoadedOK(Player jugador) {
        enviarMensajeColorido(jugador, "¡Configuración recargada con éxito!", ChatColor.GREEN);
    }

    /**
     * Sends an error message to a player indicating there was an error reloading the configuration.
     *
     * @param jugador The player to whom the message will be sent.
     */
    public static void ConfigLoadedError(Player jugador) {
        enviarMensajeColorido(jugador, "¡Error al recargar la configuración!", ChatColor.RED);
    }

    /**
     * Sends a message to all online players with a specific color.
     *
     * @param text  The message to send.
     * @param color The color of the message.
     */
    public static void sendMessageAllPlayers(String text, ChatColor color) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            enviarMensajeColorido(player, text, color);
        }
    }
}
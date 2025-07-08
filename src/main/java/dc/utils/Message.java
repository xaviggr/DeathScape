package dc.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
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
    public static void sendMessage(Player jugador, String mensaje, ChatColor color) {
        jugador.sendMessage(color + mensaje);
    }

    /**
     * Sends a confirmation message to a player indicating the configuration has been successfully reloaded.
     *
     * @param jugador The player to whom the message will be sent.
     */
    public static void ConfigLoadedOK(Player jugador) {
        sendMessage(jugador, "¡Configuración recargada con éxito!", ChatColor.GREEN);
    }

    /**
     * Sends an error message to a player indicating there was an error reloading the configuration.
     *
     * @param jugador The player to whom the message will be sent.
     */
    public static void ConfigLoadedError(Player jugador) {
        sendMessage(jugador, "¡Error al recargar la configuración!", ChatColor.RED);
    }

    /**
     * Sends a message to all online players with a specific color.
     *
     * @param text  The message to send.
     * @param color The color of the message.
     */
    public static void sendMessageAllPlayers(String text, ChatColor color) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMessage(player, text, color);
        }
    }

    /**
     * Sends a Title To all online players
     * @param title The title of the message
     * @param subtitle The subtitle of the message
     * @param fadeIn The time in ticks for the title to fade in
     * @param stay The time in ticks for the title to stay
     * @param fadeOut The time in ticks for the title to fade out
     */
    public static void sendTitleToAllPlayers(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        // Notify all online players about the death
        for (Player checkingPlayer : Bukkit.getOnlinePlayers()) {
            checkingPlayer.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * Send a Title To a specific player
     * @param player The player to send the title
     * @param title The title of the message
     * @param subtitle The subtitle of the message
     * @param fadeIn The time in ticks for the title to fade in
     * @param stay The time in ticks for the title to stay
     * @param fadeOut The time in ticks for the title to fade out
     */
    public static void sendTitleToPlayer(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    /**
     * Sends a sound effect to all online players.
     *
     * @param sound The sound effect to play.
     */
    public static void playSoundToAllPlayers(Sound sound) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, 1, 1);
        }
    }
}
package dc.utils;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public class Message {
    public static void enviarMensajeColorido(Player jugador, String mensaje, ChatColor color) {
        jugador.sendMessage(color + mensaje);
    }

    public static void ConfigLoadedOK(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Configuración recargada!", ChatColor.GREEN);
    }

    public static void ConfigLoadedError(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Error al recargar la configuración!", ChatColor.RED);
    }

    public static void sendMessageAllPlayers(String text, ChatColor color) {
        for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
            Message.enviarMensajeColorido(player, text, color);
        }
    }
}

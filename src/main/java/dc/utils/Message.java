package dc.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Message {
    public static void enviarMensajeColorido(Player jugador, String mensaje, String color) {
        ChatColor colorChat = switch (color.toLowerCase()) {
            case "rojo" -> ChatColor.RED;
            case "azul" -> ChatColor.BLUE;
            case "amarillo" -> ChatColor.YELLOW;
            case "verde" -> ChatColor.GREEN;
            default -> ChatColor.WHITE; // Color por defecto si no coincide con ninguno de los colores especificados
        };

        jugador.sendMessage(colorChat + mensaje);
    }

    public static void ConfigLoadedOK(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Configuración recargada!", "verde");
    }

    public static void ConfigLoadedError(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Error al recargar la configuración!", "rojo");
    }
}

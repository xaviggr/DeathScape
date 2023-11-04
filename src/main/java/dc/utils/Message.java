package dc.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Message {
    public static void enviarMensajeColorido(Player jugador, String mensaje, String color) {
        ChatColor colorChat;

        switch (color.toLowerCase()) {
            case "rojo":
                colorChat = ChatColor.RED;
                break;
            case "azul":
                colorChat = ChatColor.BLUE;
                break;
            case "amarillo":
                colorChat = ChatColor.YELLOW;
                break;
            default:
                colorChat = ChatColor.WHITE; // Color por defecto si no coincide con ninguno de los colores especificados
                break;
        }

        jugador.sendMessage(colorChat + mensaje);
    }
}

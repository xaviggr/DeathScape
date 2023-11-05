package dc;

import dc.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathScapeCommand implements CommandExecutor {
    private DeathScape plugin;
    public DeathScapeCommand(DeathScape plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        //Jugador
        Player player = (Player) sender;

        if(args.length >= 1) {
            if (args[0].equalsIgnoreCase("help")) {
                Message.enviarMensajeColorido(player, "DeathScape Comandos:", "azul");
                Message.enviarMensajeColorido(player, "/deathscape help - Muestra este mensaje", "azul");
                player.sendMessage("/deathscape info - Muestra información sobre el plugin");
                player.sendMessage("/deathscape dia - Muestra el dia actual");
                player.sendMessage("/deathscape despierto - Muestra cuanto tiempo llevas despierto");
                player.sendMessage("/deathscape discord - Muestra el link de discord");

            } else if (args[0].equalsIgnoreCase("info")) {
                //Futura implementación
            } else if (args[0].equalsIgnoreCase("dia")) {
                //Futura implementación
            } else if (args[0].equalsIgnoreCase("despierto")) {
                //Futura implementación
            } else if (args[0].equalsIgnoreCase("reload")) {
                if(plugin.getMainConfigManager().reloadConfig()) {
                    Message.enviarMensajeColorido (player, "Configuración recargada!", "verde");
                } else {
                    Message.enviarMensajeColorido (player, "Error al recargar la configuración!", "rojo");
                }
            } else if (args[0].equalsIgnoreCase("discord")) {
                //Futura implementación
            } else {
                comandoinvalido(player);
            }
        } else {
            comandoinvalido(player);
        }
        return true;
    }

    public void comandoinvalido(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Comando invalido! Escribe /deathscape help para la lista de comandos!", "rojo");
    }
}

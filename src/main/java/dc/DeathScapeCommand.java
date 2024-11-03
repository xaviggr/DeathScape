package dc;

import dc.Business.inventory.ReviveInventory;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeathScapeCommand implements CommandExecutor, TabCompleter {
    private final DeathScape plugin;
    private final ReviveInventory reviveInventory;

    public DeathScapeCommand(DeathScape plugin, ReviveInventory reviveInventory) {
        this.plugin = plugin;
        this.reviveInventory = reviveInventory;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("deathscape")) {
            if (args.length == 1) {
                List<String> options = new ArrayList<>();
                // Aquí agregas las options de autocompletado para el primer argumento del comando
                options.add("help");
                options.add("info");
                options.add("reload");
                options.add("dia");
                options.add("tiempojugado");
                options.add("tiempolluvia");
                options.add("discord");
                if (sender.isOp()) {
                    options.add("setdia");
                    options.add("quitarban");
                }
                return options;
            }
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verifica si el comando es ejecutado por un jugador
        if (!(sender instanceof Player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")) {
                Message.enviarMensajeColorido(player, "DeathScape Comandos:", "azul");
                Message.enviarMensajeColorido(player, "/deathscape help - Muestra este mensaje", "azul");
                Message.enviarMensajeColorido(player, "/deathscape info - Muestra información sobre el plugin", "azul");
                Message.enviarMensajeColorido(player, "/deathscape reload - Recarga la configuración", "azul");
                Message.enviarMensajeColorido(player, "/deathscape dia - Muestra el día actual del servidor", "azul");
                Message.enviarMensajeColorido(player, "/deathscape tiempojugado - Muestra el tiempo jugado", "azul");
                Message.enviarMensajeColorido(player, "/deathscape discord - Muestra el link de discord", "azul");
                Message.enviarMensajeColorido(player, "/deathscape tiempolluvia - Muestra el tiempo de lluvia pendiente", "azul");
            } else if (args[0].equalsIgnoreCase("tiempolluvia")) {
                int tiempoLluviaPendiente = plugin.getServerData().getStormPendingTime();
                Message.enviarMensajeColorido(player, "Tiempo de lluvia pendiente: " + tiempoLluviaPendiente + " minutos.", "verde");
            } else if (args[0].equalsIgnoreCase("info")) {
                Message.enviarMensajeColorido(player, "DeathScape V" + plugin.getDescription().getVersion() + " por " + plugin.getDescription().getAuthors(), "verde");
            } else if (args[0].equalsIgnoreCase("dia")) {
                Message.enviarMensajeColorido(player, "El día actual es: " + plugin.getServerData().getServerDays(), "verde");
            } else if (args[0].equalsIgnoreCase("tiempojugado")) {
                Message.enviarMensajeColorido(player, "Has jugado un total de: " + Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getTimePlayed(), "verde");
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (plugin.getMainConfigManager().reloadConfig()) {
                    Message.ConfigLoadedOK(player);
                } else {
                    Message.ConfigLoadedError(player);
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("discord")) {
                Message.enviarMensajeColorido(player, "Discord: https://discord.gg/Pe9wYt9bcV", "azul");
            } else if (args[0].equalsIgnoreCase("inventario")) {
                reviveInventory.openInventory(player);
            } else {
                comandoinvalido(player);
                return false;
            }
        } else if (args.length >= 1) {
            // Casos con más de un argumento
            if (player.isOp() && args[0].equalsIgnoreCase("setdia")) {
                if (setDia(args)) {
                    Message.ConfigLoadedOK(player);
                } else {
                    comandoinvalido(player);
                    return false;
                }
            } else if (player.isOp() && args[0].equalsIgnoreCase("quitarban")) {
                if(unbanPlayer(player, args)) {
                    Message.ConfigLoadedOK(player);
                } else {
                    comandoinvalido(player);
                    return false;
                }
            }
        } else {
            comandoinvalido(player);
            return false;
        }
        return true;
    }

    public boolean setDia(String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            try {
                plugin.getServerData().setServerDays(Integer.parseInt(args[1]));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    public void comandoinvalido(Player jugador) {
        Message.enviarMensajeColorido(jugador, "Comando invalido! Escribe /deathscape help para la lista de comandos!", "rojo");
    }

    public boolean unbanPlayer(Player player,String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            if (Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(args[1])) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(args[1]);
                PlayerEditDatabase.UnbanPlayer(args[1]);
                Message.enviarMensajeColorido(player, "El jugador " + args[1] + " ha sido desbaneado.", "verde");
            } else {
                Message.enviarMensajeColorido(player, "El jugador " + args[1] + " no está baneado.", "rojo");
            }
            return true;
        }
    }
}

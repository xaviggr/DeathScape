package dc;

import dc.Business.inventory.ReportInventory;
import dc.Business.inventory.ReportsInventory;
import dc.Business.inventory.ReviveInventory;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final ReportInventory reportInventory;
    private final ReportsInventory reportsInventory;

    public DeathScapeCommand(DeathScape plugin, ReportInventory reportInventory, ReportsInventory reportsInventory) {
        this.plugin = plugin;
        this.reportInventory = reportInventory;
        this.reportsInventory = reportsInventory;
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
                options.add("reportar");
                if (sender.isOp()) {
                    options.add("setdia");
                    options.add("quitarban");
                    options.add("añadirBannedWord");
                    options.add("quitarBannedWord");
                    options.add("reportes");
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
                Message.enviarMensajeColorido(player, "DeathScape Comandos:", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape help - Muestra este mensaje", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape info - Muestra información sobre el plugin", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape reload - Recarga la configuración", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape dia - Muestra el día actual del servidor", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape tiempojugado - Muestra el tiempo jugado", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape discord - Muestra el link de discord", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape tiempolluvia - Muestra el tiempo de lluvia pendiente", ChatColor.BLUE);
                Message.enviarMensajeColorido(player, "/deathscape reportar - Abre el menú de reportes", ChatColor.BLUE);
            } else if (args[0].equalsIgnoreCase("tiempolluvia")) {
                int tiempoLluviaPendiente = plugin.getServerData().getStormPendingTime();
                Message.enviarMensajeColorido(player, "Tiempo de lluvia pendiente: " + tiempoLluviaPendiente + " minutos.", ChatColor.GREEN);
            } else if (args[0].equalsIgnoreCase("info")) {
                Message.enviarMensajeColorido(player, "DeathScape V" + plugin.getDescription().getVersion() + " por " + plugin.getDescription().getAuthors(), ChatColor.GREEN);
            } else if (args[0].equalsIgnoreCase("dia")) {
                Message.enviarMensajeColorido(player, "El día actual es: " + plugin.getServerData().getServerDays(), ChatColor.GREEN);
            } else if (args[0].equalsIgnoreCase("tiempojugado")) {
                Message.enviarMensajeColorido(player, "Has jugado un total de: " + Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getTimePlayed(), ChatColor.GREEN);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (plugin.getMainConfigManager().reloadConfig()) {
                    Message.ConfigLoadedOK(player);
                } else {
                    Message.ConfigLoadedError(player);
                    return false;
                }
            } else if (args[0].equalsIgnoreCase("discord")) {
                Message.enviarMensajeColorido(player, "Discord: https://discord.gg/Pe9wYt9bcV", ChatColor.BLUE);
            } else if (args[0].equalsIgnoreCase("reportar")) {
                reportInventory.openInventory(player);
            } else if (player.isOp() && args[0].equalsIgnoreCase("reportes")) {
                reportsInventory.openInventory(player);
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
                if (unbanPlayer(player, args)) {
                    Message.ConfigLoadedOK(player);
                } else {
                    comandoinvalido(player);
                    return false;
                }
            } else if (player.isOp() && args[0].equalsIgnoreCase("añadirBannedWord")) {
                BannedWordsDatabase.addBannedWord(args[1]);
                Message.enviarMensajeColorido(player, "La palabra " + args[1] + " ha sido añadida a la lista de palabras prohibidas.", ChatColor.GREEN);
            } else if (player.isOp() && args[0].equalsIgnoreCase("quitarBannedWord")) {
                BannedWordsDatabase.removeBannedWord(args[1]);
                Message.enviarMensajeColorido(player, "La palabra " + args[1] + " ha sido eliminada de la lista de palabras prohibidas.", ChatColor.GREEN);
            } else {
                comandoinvalido(player);
                return false;
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
        Message.enviarMensajeColorido(jugador, "Comando invalido! Escribe /deathscape help para la lista de comandos!", ChatColor.RED);
    }

    public boolean unbanPlayer(Player player,String[] args) {
        if (args.length < 2) {
            return false;
        } else {
            if (Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(args[1])) {
                Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(args[1]);
                PlayerEditDatabase.UnbanPlayer(args[1]);
                Message.enviarMensajeColorido(player, "El jugador " + args[1] + " ha sido desbaneado.", ChatColor.GREEN);
            } else {
                Message.enviarMensajeColorido(player, "El jugador " + args[1] + " no está baneado.", ChatColor.RED);
            }
            return true;
        }
    }
}

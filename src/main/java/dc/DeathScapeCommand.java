package dc;

import dc.Business.controllers.PlayerController;
import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
import dc.Business.inventory.ReportInventory;
import dc.Business.inventory.ReportsInventory;
import dc.Business.player.PlayerData;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeathScapeCommand implements CommandExecutor, TabCompleter {
    private static final String DISCORD_URL = "https://discord.gg/Pe9wYt9bcV";
    private static final String INVALID_COMMAND_MESSAGE = "Comando invalido! Escribe /deathscape help para la lista de comandos!";

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final ReportInventory reportInventory;
    private final ReportsInventory reportsInventory;

    public DeathScapeCommand(DeathScape plugin, ReportInventory reportInventory, ReportsInventory reportsInventory, PlayerController playerController) {
        this.plugin = plugin;
        this.reportInventory = reportInventory;
        this.reportsInventory = reportsInventory;
        this.playerController = playerController;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("deathscape") && args.length == 1) {
            List<String> options = new ArrayList<>(List.of("dia", "discord", "help", "info", "reportar","tiempojugado", "tiempolluvia"));

            if (sender instanceof Player player) {
                GroupData groupData = GroupDatabase.getGroupData(Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getGroup());

                if (groupData != null) {
                    List<String> groupPermissions = groupData.getPermissions().stream()
                            .map(Permission::toString)
                            .toList();
                    if (groupPermissions.contains("group")) {
                        options.add("añadirUsuarioAGrupo");
                        options.add("quitarUsuarioDeGrupo");
                    }

                    // Add specific commands based on group permissions
                    if (groupPermissions.contains("ban")) {
                        options.add("quitarban");
                    }
                    if (groupPermissions.contains("reload")) {
                        options.add("reload");
                    }
                    if (groupPermissions.contains("reports")) {
                        options.add("reportes");
                    }
                    if (groupPermissions.contains("banshee")) {
                        options.add("banshee");
                    }
                    if (groupPermissions.contains("days")) {
                        options.add("setdia");
                    }
                    if (groupPermissions.contains("chat")) {
                        options.add("añadirbannedword");
                        options.add("quitarbannedword");
                    }
                }
            }
            return options;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede ser usado por jugadores.");
            return true;
        }

        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            sender.sendMessage(ChatColor.RED + "No se pudo encontrar los datos del jugador.");
            return true;
        }

        GroupData group = GroupDatabase.getGroupData(playerData.getGroup());
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "No tienes un grupo asignado.");
            return true;
        }

        // Verificar los permisos según los comandos
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "help":
                    showHelp(player);
                    break;
                case "tiempolluvia":
                    showStormTime(player);
                    break;
                case "info":
                    showInfo(player);
                    break;
                case "reportar":
                    reportInventory.openInventory(player);
                    break;
                case "reportes":
                    if (group.getPermissions().contains(Permission.REPORTS)) {
                        reportsInventory.openInventory(player);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                case "reload":
                    if (group.getPermissions().contains(Permission.RELOAD)) {
                        reloadConfig(player);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                case "banshee":
                    if (group.getPermissions().contains(Permission.BANSHEE)) {
                        if (playerController.isBansheeActive(player)) {
                            playerController.deactivateBanshee(player);
                            Message.enviarMensajeColorido(player, "Banshee desactivado.", ChatColor.GREEN);
                        } else {
                            playerController.activateBanshee(player);
                            Message.enviarMensajeColorido(player, "Banshee activado.", ChatColor.GREEN);
                        }
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;

                case "discord":
                    showDiscord(player);
                    break;
                case "tiempojugado":
                    showPlayTime(player);
                    break;
                case "dia":
                    showDay(player);
                    break;
                default:
                    sendInvalidCommandMessage(player);
                    return false;
            }
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "setdia":
                    if (group.getPermissions().contains(Permission.DAYS)) {
                        setDay(player, args[1]);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                case "quitarban":
                    if (group.getPermissions().contains(Permission.BAN)) {
                        unbanPlayer(player, args[1]);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                case "añadirbannedword":
                    if (group.getPermissions().contains(Permission.CHAT)) {
                        addBannedWord(player, args[1]);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                case "quitarbannedword":
                    if (group.getPermissions().contains(Permission.CHAT)) {
                        removeBannedWord(player, args[1]);
                    } else {
                        sendNoPermissionMessage(player);
                    }
                    break;
                default:
                    sendInvalidCommandMessage(player);
                    return false;
            }
        } else if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("añadirusuarioagrupo")) {
                if (group.getPermissions().contains(Permission.GROUP)) {
                    PlayerEditDatabase.addPlayerToGroup(args[1], args[2]);
                } else {
                    sendNoPermissionMessage(player);
                }
            } else if (args[0].equalsIgnoreCase("quitarusuariodegrupo")) {
                if (group.getPermissions().contains(Permission.GROUP)) {
                    PlayerEditDatabase.removePlayerFromGroup(args[1]);
                } else {
                    sendNoPermissionMessage(player);
                }
            }
        } else {
            sendInvalidCommandMessage(player);
            return false;
        }
        return true;
    }

    private void showHelp(Player player) {
        String[] helpMessages = {
                "DeathScape Comandos:",
                "/deathscape dia - Muestra el día actual del servidor",
                "/deathscape discord - Muestra el link de discord",
                "/deathscape help - Muestra este mensaje",
                "/deathscape info - Muestra información sobre el plugin",
                "/deathscape reload - Recarga la configuración",
                "/deathscape reportar - Abre el menú de reportes",
                "/deathscape tiempojugado - Muestra el tiempo jugado",
                "/deathscape tiempolluvia - Muestra el tiempo de lluvia pendiente"
        };
        for (String msg : helpMessages) {
            Message.enviarMensajeColorido(player, msg, ChatColor.BLUE);
        }
    }

    private void showInfo(Player player) {
        String infoMessage = "DeathScape V" + plugin.getDescription().getVersion() + " por " + plugin.getDescription().getAuthors();
        Message.enviarMensajeColorido(player, infoMessage, ChatColor.GREEN);
    }

    private void showDiscord(Player player) {
        Message.enviarMensajeColorido(player, "Discord: " + DISCORD_URL, ChatColor.BLUE);
    }

    private void showStormTime(Player player) {
        int tiempoLluviaPendiente = plugin.getServerData().getStormPendingTime();
        Message.enviarMensajeColorido(player, "Tiempo de lluvia pendiente: " + tiempoLluviaPendiente + " minutos.", ChatColor.GREEN);
    }

    private void showDay(Player player) {
        Message.enviarMensajeColorido(player, "El día actual es: " + plugin.getServerData().getServerDays(), ChatColor.GREEN);
    }

    private void showPlayTime(Player player) {
        String tiempoJugado = Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getTimePlayed();
        Message.enviarMensajeColorido(player, "Has jugado un total de: " + tiempoJugado, ChatColor.GREEN);
    }

    private void reloadConfig(Player player) {
        if (MainConfigManager.getInstance().reloadConfig()) {
            Message.ConfigLoadedOK(player);
        } else {
            Message.ConfigLoadedError(player);
        }
    }

    private void setDay(Player player, String day) {
        try {
            plugin.getServerData().setServerDays(Integer.parseInt(day));
            Message.ConfigLoadedOK(player);
        } catch (NumberFormatException e) {
            sendInvalidCommandMessage(player);
        }
    }

    private void unbanPlayer(Player player, String playerName) {
        if (Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(playerName)) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
            PlayerEditDatabase.UnbanPlayer(playerName);
            Message.enviarMensajeColorido(player, "El jugador " + playerName + " ha sido desbaneado.", ChatColor.GREEN);
        } else {
            Message.enviarMensajeColorido(player, "El jugador " + playerName + " no está baneado.", ChatColor.RED);
        }
    }

    private void addBannedWord(Player player, String word) {
        BannedWordsDatabase.addBannedWord(word);
        Message.enviarMensajeColorido(player, "La palabra " + word + " ha sido añadida a la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    private void removeBannedWord(Player player, String word) {
        BannedWordsDatabase.removeBannedWord(word);
        Message.enviarMensajeColorido(player, "La palabra " + word + " ha sido eliminada de la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    private void sendInvalidCommandMessage(Player player) {
        Message.enviarMensajeColorido(player, INVALID_COMMAND_MESSAGE, ChatColor.RED);
    }

    private void sendNoPermissionMessage(Player player) {
        Message.enviarMensajeColorido(player, "No tienes permiso para usar este comando.", ChatColor.RED);
    }
}

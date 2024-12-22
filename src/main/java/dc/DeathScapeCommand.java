package dc;

import dc.Business.controllers.PlayerController;
import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
import dc.Business.inventory.ReportInventory;
import dc.Business.inventory.ReportsInventory;
import dc.Business.inventory.ReviveInventory;
import dc.Business.log.LogData;
import dc.Business.player.PlayerData;
import dc.Persistence.chat.BannedWordsDatabase;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.logs.LogDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class DeathScapeCommand implements CommandExecutor, TabCompleter {
    private static final String DISCORD_URL = "https://discord.gg/Pe9wYt9bcV";
    private static final String INVALID_COMMAND_MESSAGE = "Comando invalido! Escribe /deathscape help para la lista de comandos!";

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final ReportInventory reportInventory;
    private final ReportsInventory reportsInventory;
    private final ReviveInventory reviveInventory;

    public DeathScapeCommand(DeathScape plugin, ReportInventory reportInventory, ReportsInventory reportsInventory, PlayerController playerController, ReviveInventory reviveInventory) {
        this.plugin = plugin;
        this.reportInventory = reportInventory;
        this.reportsInventory = reportsInventory;
        this.playerController = playerController;
        this.reviveInventory = reviveInventory;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("deathscape") && args.length == 1) {
            // Lista base de comandos
            List<String> options = new ArrayList<>(List.of(
                    "dia", "discord", "help", "info", "reportar", "tiempojugado",
                    "tiempolluvia", "dificultad"
            ));

            if (sender instanceof Player player) {
                // Obtener datos del grupo del jugador
                GroupData groupData = GroupDatabase.getGroupData(
                        Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getGroup()
                );

                if (groupData != null) {
                    List<String> groupPermissions = groupData.getPermissions().stream()
                            .map(Permission::toString)
                            .toList();

                    // Añadir comandos según los permisos
                    if (groupPermissions.contains("group") || player.isOp()) {
                        options.add("añadirUsuarioAGrupo");
                        options.add("quitarUsuarioDeGrupo");
                        options.add("inventorysee");
                        options.add("endersee");
                    }

                    if (groupPermissions.contains("teleport")) {
                        options.add("tp");
                        options.add("back");
                    }

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

                    if (groupPermissions.contains("heal")) {
                        options.add("heal");
                    }

                    if (groupPermissions.contains("days")) {
                        options.add("setdia");
                    }

                    if (groupPermissions.contains("chat")) {
                        options.add("añadirbannedword");
                        options.add("quitarbannedword");
                    }

                    if (groupPermissions.contains("mute")) {
                        options.add("mute");
                        options.add("unmute");
                    }
                }
            }

            // Filtrar los comandos según la entrada del usuario
            String input = args[0].toLowerCase();
            List<String> filteredOptions = new ArrayList<>();
            for (String option : options) {
                if (option.toLowerCase().startsWith(input)) {
                    filteredOptions.add(option);
                }
            }

            return filteredOptions;
        }
        return null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            if (args[0].equalsIgnoreCase("revive")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
                } else {
                    reviveInventory.openInventory(target);
                }
            } else if (args[0].equalsIgnoreCase("addPoints")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || !target.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
                } else {
                    playerController.addPointsToPlayer(target, Integer.parseInt(args[2]));
                }

            } else {
                sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            }
            return true;
        }

        // Verificación de datos del jugador y su grupo
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null || !validateGroup(player, playerData)) {
            return true;
        }

        GroupData group = GroupDatabase.getGroupData(playerData.getGroup());
        if (group == null) {
            sender.sendMessage(ChatColor.RED + "No tienes un grupo asignado.");
            return true;
        }

        if (args.length == 0) {
            sendInvalidCommandMessage(player);
            return false;
        }

        // Mapa de comandos y sus respectivos manejadores
        Runnable commandAction = getRunnable(args, player, group);
        if (commandAction != null) {
            commandAction.run();
        } else {
            sendInvalidCommandMessage(player);
        }

        LogDatabase.addLog(new LogData("command", player.getName(), Arrays.toString(args)));

        return true;
    }

    private Runnable getRunnable(String[] args, Player player, GroupData group) {
        Map<String, Runnable> commandMap = new HashMap<>();
        commandMap.put("help", () -> showHelp(player));
        commandMap.put("tiempolluvia", () -> showStormTime(player));
        commandMap.put("info", () -> showInfo(player));
        commandMap.put("reportar", () -> handleMakeReport(player));
        commandMap.put("reportes", () -> handleShowReports(player, group));
        commandMap.put("reload", () -> handleReloadConfig(player, group));
        commandMap.put("banshee", () -> handleBansheeCommand(player, group));
        commandMap.put("discord", () -> showDiscord(player));
        commandMap.put("tiempojugado", () -> showPlayTime(player));
        commandMap.put("dia", () -> showDay(player));
        commandMap.put("heal", () -> playerController.healPlayer(player));
        commandMap.put("back", () -> handleBackCommand(player, group));
        commandMap.put("añadirusuarioagrupo", () -> handleAddUserToGroupCommand(player, args, group));
        commandMap.put("quitarusuariodegrupo", () -> handleRemoveUserFromGroupCommand(player, args, group));
        commandMap.put("tp", () -> handleTeleportCommand(player, args, group));
        commandMap.put("setdia", () -> handleSetDayCommand(player, args, group));
        commandMap.put("añadirbannedword", () -> handleAddBannedWordCommand(player, args, group));
        commandMap.put("quitarbannedword", () -> handleRemoveBannedWordCommand(player, args, group));
        commandMap.put("quitarban", () -> handleUnbanPlayerCommand(player, args, group));
        commandMap.put("dificultad", () -> handleDifficultyCommand(player, args));
        commandMap.put("inventorysee", () -> handleInventorySee(player, args, group));
        commandMap.put("endersee", () -> handleEnderSee(player, args, group));
        commandMap.put("mute", () -> handleMutePlayer(player, args, group));
        commandMap.put("unmute", () -> handleUnMutePlayer(player, args, group));

        // Ejecuta el comando correspondiente
        return commandMap.get(args[0].toLowerCase());
    }

    private void handleUnMutePlayer(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.MUTE)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /unmute <nombre del jugador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
            return;
        }

        playerController.unmutePlayer(player, target);
    }

    private void handleMutePlayer(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.MUTE)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /mute <nombre del jugador> <duración en minutos>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
            return;
        }

        int duration = Integer.parseInt(args[2]);

        playerController.mutePlayer(player, target, duration);
    }

    private void handleEnderSee(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /endersee <nombre del jugador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
            return;
        }

        player.openInventory(target.getEnderChest());
        player.sendMessage(ChatColor.GREEN + "Estás viendo el cofre de Ender de " + target.getName() + ".");
    }

    private void handleInventorySee(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /inventorysee <nombre del jugador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
            return;
        }

        player.openInventory(target.getInventory());
        player.sendMessage(ChatColor.GREEN + "Estás viendo el inventario de " + target.getName() + ".");
    }

    private boolean validateGroup(Player player, PlayerData playerData) {
        GroupData group = GroupDatabase.getGroupData(playerData.getGroup());
        if (group == null) {
            player.sendMessage(ChatColor.RED + "No tienes un grupo asignado.");
            return false;
        }
        return true;
    }

    private void handleMakeReport(Player player) {
        reportInventory.openInventory(player);
    }

    private void handleShowReports(Player player, GroupData group) {
        if (group.getPermissions().contains(Permission.REPORTS)) {
            reportsInventory.openInventory(player);
        } else {
            sendNoPermissionMessage(player);
        }
    }

    private void handleReloadConfig(Player player, GroupData group) {
        if (group.getPermissions().contains(Permission.RELOAD)) {
            reloadConfig(player);
        } else {
            sendNoPermissionMessage(player);
        }
    }

    private void handleTeleportCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.TELEPORT)) {
            sendNoPermissionMessage(player);
            return;
        }

        switch (args.length) {
            case 2 -> {
                if (args[1].equalsIgnoreCase("lobby")) {
                    player.teleport(Objects.requireNonNull(plugin.getServer().getWorld("world")).getSpawnLocation());
                    return;
                }
                // Teletransportarse a un jugador
                if (playerController.teleportToPlayer(player, args[1])) {
                    Message.enviarMensajeColorido(player, "Te has teletransportado a " + args[1], ChatColor.GREEN);
                } else {
                    Message.enviarMensajeColorido(player, "El jugador " + args[1] + " no está conectado.", ChatColor.RED);
                }
            }
            case 4 -> {
                // Teletransportarse a coordenadas
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    if (playerController.teleportToCoordinates(player, x, y, z)) {
                        Message.enviarMensajeColorido(player, "Te has teletransportado a las coordenadas: " + x + ", " + y + ", " + z, ChatColor.GREEN);
                    }
                } catch (NumberFormatException e) {
                    Message.enviarMensajeColorido(player, "Por favor, introduce coordenadas válidas.", ChatColor.RED);
                }
            }
            case 3 -> {
                // Teletransportar a un jugador a otro
                if (playerController.teleportPlayerToPlayer(args[1], args[2])) {
                    Message.enviarMensajeColorido(player, "Has teletransportado a " + args[1] + " a " + args[2], ChatColor.GREEN);
                } else {
                    Message.enviarMensajeColorido(player, "No se pudo completar el teletransporte.", ChatColor.RED);
                }
            }
            default -> Message.enviarMensajeColorido(player, """
                Uso incorrecto del comando. Formatos válidos:
                /tp <jugador>
                /tp <x> <y> <z>
                /tp <jugador_origen> <jugador_destino>
                """, ChatColor.RED);
        }
    }

    private void handleBansheeCommand(Player player, GroupData group) {
        if (!group.getPermissions().contains(Permission.BANSHEE)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (playerController.isBansheeActive(player)) {
            playerController.deactivateBanshee(player);
            Message.enviarMensajeColorido(player, "Banshee desactivado.", ChatColor.GREEN);
        } else {
            playerController.activateBanshee(player);
            Message.enviarMensajeColorido(player, "Banshee activado.", ChatColor.GREEN);
        }
    }

    private void handleAddUserToGroupCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP) && !player.isOp()) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 3) {
            Message.enviarMensajeColorido(player, "Uso: /añadirusuarioagrupo <usuario> <grupo>", ChatColor.RED);
            return;
        }

        PlayerEditDatabase.addPlayerToGroup(args[1], args[2]);
        Message.enviarMensajeColorido(player, "Usuario añadido al grupo correctamente.", ChatColor.GREEN);
    }

    private void handleRemoveUserFromGroupCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP) && !player.isOp()) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /quitarusuariodegrupo <usuario>", ChatColor.RED);
            return;
        }

        PlayerEditDatabase.addPlayerToGroup(args[1], "default");
        Message.enviarMensajeColorido(player, "Usuario eliminado del grupo correctamente.", ChatColor.GREEN);
    }

    private void handleBackCommand(Player player, GroupData group) {
        if (!group.getPermissions().contains(Permission.TELEPORT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (playerController.teleportBack(player)) {
            Message.enviarMensajeColorido(player, "Te has teletransportado al punto anterior.", ChatColor.GREEN);
        } else {
            Message.enviarMensajeColorido(player, "No hay un punto anterior al que teletransportarse.", ChatColor.RED);
        }
    }

    private void handleSetDayCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.DAYS)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /setdia <número de días>", ChatColor.RED);
            return;
        }

        try {
            int days = Integer.parseInt(args[1]);
            try {
                plugin.getServerData().setServerDays(days);
                Message.ConfigLoadedOK(player);
            } catch (NumberFormatException e) {
                sendInvalidCommandMessage(player);
            }
            Message.enviarMensajeColorido(player, "Se ha establecido el día a " + days + " correctamente.", ChatColor.GREEN);
        } catch (NumberFormatException e) {
            Message.enviarMensajeColorido(player, "Por favor, introduce un número válido de días.", ChatColor.RED);
        }
    }

    private void handleAddBannedWordCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.CHAT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /añadirbannedword <palabra>", ChatColor.RED);
            return;
        }

        String word = args[1];
        BannedWordsDatabase.addBannedWord(word);
        Message.enviarMensajeColorido(player, "La palabra " + word + " ha sido añadida a la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    private void handleRemoveBannedWordCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.CHAT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /quitarbannedword <palabra>", ChatColor.RED);
            return;
        }

        String word = args[1];
        BannedWordsDatabase.removeBannedWord(word);
        Message.enviarMensajeColorido(player, "La palabra " + word + " ha sido eliminada de la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    private void handleUnbanPlayerCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.BAN)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /quitarban <usuario>", ChatColor.RED);
            return;
        }

        String targetPlayer = args[1];
        if (Bukkit.getBanList(org.bukkit.BanList.Type.NAME).isBanned(targetPlayer)) {
            Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(targetPlayer);
            PlayerEditDatabase.UnbanPlayer(targetPlayer);
            Message.enviarMensajeColorido(player, "El jugador " + targetPlayer + " ha sido desbaneado.", ChatColor.GREEN);
        } else {
            Message.enviarMensajeColorido(player, "El jugador " + targetPlayer + " no está baneado.", ChatColor.RED);
        }
    }

    private void showHelp(Player player) {
        // Obtener datos del grupo del jugador
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            Message.enviarMensajeColorido(player, "No se pudo cargar tu información de grupo.", ChatColor.RED);
            return;
        }

        GroupData groupData = GroupDatabase.getGroupData(playerData.getGroup());
        if (groupData == null) {
            Message.enviarMensajeColorido(player, "No tienes un grupo asignado.", ChatColor.RED);
            return;
        }

        List<String> groupPermissions = groupData.getPermissions().stream()
                .map(Permission::toString)
                .toList();

        // Crear un mapa de comandos y descripciones
        Map<String, String> commands = new LinkedHashMap<>();
        commands.put("dia", "Muestra el día actual del servidor.");
        commands.put("discord", "Muestra el enlace del servidor de Discord.");
        commands.put("help", "Muestra esta lista de comandos.");
        commands.put("info", "Muestra información sobre el plugin.");
        commands.put("reportar", "Abre el menú de reportes.");
        commands.put("tiempojugado", "Muestra el tiempo jugado.");
        commands.put("tiempolluvia", "Muestra el tiempo de lluvia pendiente.");
        commands.put("dificultad", "Muestra información sobre la dificultad de un día específico.");

        // Comandos adicionales según permisos
        if (groupPermissions.contains("group") || player.isOp()) {
            commands.put("añadirUsuarioAGrupo", "Añade un usuario a un grupo.");
            commands.put("quitarUsuarioDeGrupo", "Quita a un usuario de un grupo.");
            commands.put("inventorysee", "Permite ver el inventario de otro jugador.");
            commands.put("endersee", "Permite ver el Ender Chest de otro jugador.");
        }

        if (groupPermissions.contains("teleport")) {
            commands.put("tp", "Teletransporta a un jugador o a una ubicación.");
            commands.put("back", "Te teletransporta a tu última ubicación.");
        }

        if (groupPermissions.contains("ban")) {
            commands.put("quitarban", "Quita el ban a un jugador.");
        }

        if (groupPermissions.contains("reload")) {
            commands.put("reload", "Recarga la configuración del plugin.");
        }

        if (groupPermissions.contains("reports")) {
            commands.put("reportes", "Abre el menú de reportes recibidos.");
        }

        if (groupPermissions.contains("banshee")) {
            commands.put("banshee", "Activa o desactiva el modo Banshee.");
        }

        if (groupPermissions.contains("heal")) {
            commands.put("heal", "Cura al jugador.");
        }

        if (groupPermissions.contains("days")) {
            commands.put("setdia", "Establece el día actual del servidor.");
        }

        if (groupPermissions.contains("chat")) {
            commands.put("añadirbannedword", "Añade una palabra prohibida al chat.");
            commands.put("quitarbannedword", "Quita una palabra prohibida del chat.");
        }

        if (groupPermissions.contains("mute")) {
            commands.put("mute", "Silencia a un jugador.");
            commands.put("unmute", "Quita el silencio a un jugador.");
        }

        // Mostrar los comandos y descripciones al jugador
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "Comandos disponibles:");
        for (Map.Entry<String, String> entry : commands.entrySet()) {
            player.sendMessage(ChatColor.GREEN + "/" + entry.getKey() + ChatColor.WHITE + " - " + entry.getValue());
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
        int rainTimePending = plugin.getServerData().getStormPendingTime();
        Message.enviarMensajeColorido(player, "Tiempo de lluvia pendiente: " + rainTimePending + " minutos.", ChatColor.GREEN);
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

    private void sendInvalidCommandMessage(Player player) {
        Message.enviarMensajeColorido(player, INVALID_COMMAND_MESSAGE, ChatColor.RED);
    }

    private void sendNoPermissionMessage(Player player) {
        Message.enviarMensajeColorido(player, "No tienes permiso para usar este comando.", ChatColor.RED);
    }

    private void handleDifficultyCommand(Player player, String[] args) {
        if (args.length < 2) {
            Message.enviarMensajeColorido(player, "Uso: /dificultad <día>", ChatColor.RED);
            return;
        }

        // Convertir el argumento del día solicitado a un entero
        int requestedDay;
        try {
            requestedDay = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Message.enviarMensajeColorido(player, "El día ingresado no es válido. Por favor, ingrese un número.", ChatColor.RED);
            return;
        }

        int currentDay = plugin.getServerData().getServerDays();

        // Comparar el día solicitado con el día actual
        if (requestedDay > currentDay) {
            Message.enviarMensajeColorido(player, "No puedes solicitar información para un día superior al actual (" + currentDay + ").", ChatColor.RED);
            return;
        }

        // Clave esperada en el archivo difficulties.yml
        String dayKey = "day_" + requestedDay;
        FileConfiguration difficultiesConfig = plugin.getDifficultiesConfig(); // Usamos el método del plugin

        // Validar si la sección "dificultades_info" existe
        ConfigurationSection difficultiesSection = difficultiesConfig.getConfigurationSection("dificultades_info");
        if (difficultiesSection == null) {
            Message.enviarMensajeColorido(player, "No se encontró la sección 'dificultades_info' en el archivo de configuración.", ChatColor.RED);
            return;
        }

        // Validar si la clave específica del día existe
        if (!difficultiesSection.contains(dayKey)) {
            Message.enviarMensajeColorido(player, "No se encontró información para el día: " + requestedDay, ChatColor.RED);
            return;
        }

        // Obtener la lista de líneas para la clave del día
        List<String> infoLines = difficultiesSection.getStringList(dayKey);
        if (infoLines.isEmpty()) {
            Message.enviarMensajeColorido(player, "No hay detalles disponibles para el día: " + requestedDay, ChatColor.RED);
            return;
        }

        // Enviar los detalles al jugador
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "Dificultades" + ChatColor.DARK_GREEN + " para el día " + requestedDay + ":");
        for (String line : infoLines) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

}
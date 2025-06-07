package dc;

import dc.Business.controllers.DungeonController;
import dc.Business.controllers.LifeController;
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
import dc.Persistence.leaderboard.LeaderboardExporter;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.Persistence.stash.PlayerStashDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import dc.Business.menu.MainMenu;

import java.util.*;

/**
 * Handles commands and tab completion for the DeathScape plugin.
 */
public class DeathScapeCommand implements CommandExecutor, TabCompleter {
    private static final String DISCORD_URL = "https://discord.gg/Pe9wYt9bcV";
    private static final String INVALID_COMMAND_MESSAGE = "Comando invalido! Escribe /deathscape help para la lista de comandos!";

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final ReportInventory reportInventory;
    private final ReportsInventory reportsInventory;
    private final ReviveInventory reviveInventory;

    private final DungeonController dungeonController;
    private final MainMenu mainMenu;

    /**
     * Constructor for the DeathScapeCommand class.
     *
     * @param plugin           The main plugin instance.
     * @param reportInventory  The inventory used for reporting.
     * @param reportsInventory The inventory used to view reports.
     * @param playerController The player controller for managing players.
     * @param reviveInventory  The inventory used for reviving players.
     * @param lifeController
     */
    public DeathScapeCommand(DeathScape plugin, ReportInventory reportInventory, ReportsInventory reportsInventory, PlayerController playerController, ReviveInventory reviveInventory, DungeonController dungeonController, LifeController lifeController) {
        this.plugin = plugin;
        this.reportInventory = reportInventory;
        this.reportsInventory = reportsInventory;
        this.playerController = playerController;
        this.reviveInventory = reviveInventory;
        this.dungeonController = dungeonController;
        this.mainMenu = new MainMenu(plugin);
    }

    /**
     * Provides tab completion for the "/deathscape" command.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param alias   The alias used for the command.
     * @param args    The arguments provided by the user.
     * @return A list of suggested completions for the command.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("deathscape") && args.length == 1) {
            // Lista base de comandos
            List<String> options = new ArrayList<>(List.of(
                    "menu", "dia", "discord", "help", "info", "reportar", "tiempojugado",
                    "tiempolluvia", "dificultad", "vidas", "leaderboard", "puntos", "alijo"
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
                        options.add("leaderboard");
                        options.add("addvidas");
                        options.add("removevidas");
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

    /**
     * Handles the execution of the "/deathscape" command.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias used for the command.
     * @param args    The arguments provided by the user.
     * @return true if the command was handled successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            // Handle server commands using the server-specific command map
            if (args.length == 0) {
                sender.sendMessage(ChatColor.RED + "Invalid command! Use /deathscape help for a list of commands.");
                return false;
            }

            Runnable serverCommandAction = getServerRunnable(args, sender);
            if (serverCommandAction != null) {
                serverCommandAction.run();
            } else {
                sender.sendMessage(ChatColor.RED + "Unknown server command. Use /deathscape help for valid commands.");
            }
            return true;
        }

        // Verifications for player commands
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

        // Map commands to their corresponding actions
        Runnable commandAction = getRunnable(args, player, group);
        if (commandAction != null) {
            commandAction.run();
        } else {
            sendInvalidCommandMessage(player);
        }

        LogDatabase.addLog(new LogData("command", player.getName(), Arrays.toString(args)));

        return true;
    }

    /**
     * Maps commands to their corresponding actions.
     *
     * @param args   The arguments provided by the user.
     * @param player The player executing the command.
     * @param group  The group data of the player.
     * @return The action to be executed, or null if the command is invalid.
     */
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
        commandMap.put("heal", () -> handleHealCommand(player, group, args));
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
        commandMap.put("dungeon", () -> dungeonController.teleportPlayerToDungeon(player));
        commandMap.put("addvidas", () -> handleAddVidasCommand(player, args));
        commandMap.put("removevidas", () -> handleQuitarVidasCommand(player, args));
        commandMap.put("vidas", () -> handleVidasCommand(player, args));
        commandMap.put("leaderboard", () -> handleLeaderboard(player, args));
        commandMap.put("puntos", () -> handlePoints(player, args));
        commandMap.put("alijo", () -> handlePlayerStash(player, args));
        commandMap.put("menu", () -> mainMenu.openMainMenu(player));

        // Ejecuta el comando correspondiente
        return commandMap.get(args[0].toLowerCase());
    }

    /**
     * Maps server commands to their corresponding actions.
     * @param args The arguments provided by the user.
     * @param sender The command sender.
     * @return The action to be executed, or null if the command is invalid.
     */
    private Runnable getServerRunnable(String[] args, CommandSender sender) {
        Map<String, Runnable> commandMap = new HashMap<>();

        commandMap.put("revive", () -> handleReviveCommand(args, sender));
        commandMap.put("addpoints", () -> handleAddPointsCommand(args, sender));
        commandMap.put("quitarban", () -> handleUnbanPlayerCommandServer(sender, args));
        commandMap.put("leaderboard", () -> handleLeaderboardServer(sender, args));
        commandMap.put("addvidas", () -> handleAddVidasCommand(sender, args));
        commandMap.put("removevidas", () -> handleQuitarVidasCommand(sender, args));

        // Add more server commands here as needed...

        return commandMap.get(args[0].toLowerCase());
    }

    private void handleUnbanPlayerCommandServer(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /quitarban <player>");
            return;
        }

        String targetPlayer = args[1];
        if (PlayerEditDatabase.isPlayerBanned(targetPlayer)) {
            PlayerEditDatabase.UnbanPlayer(targetPlayer);
            sender.sendMessage(ChatColor.GREEN + "Player " + targetPlayer + " has been unbanned.");
        } else {
            sender.sendMessage(ChatColor.RED + "Player " + targetPlayer + " is not banned.");
        }
    }

    /**
     * Method to handle the addition of points to a player.
     * @param args The arguments provided by the user.
     * @param sender The command sender.
     */
    private void handleAddPointsCommand(String[] args, CommandSender sender) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /addPoints <player> <amount>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "The specified player is not online.");
        } else {
            try {
                int points = Integer.parseInt(args[2]);
                playerController.addPointsToPlayer(target, points);
                sender.sendMessage(ChatColor.GREEN + "Added " + points + " points to " + target.getName() + ".");
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid points amount. Please enter a number.");
            }
        }
    }

    /**
     * Method that handles the "/revive" command.
     * @param args The arguments provided by the user.
     * @param sender The command sender.
     */
    private void handleReviveCommand(String[] args, CommandSender sender) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /revive <player>");
            return;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "The specified player is not online.");
        } else {
            reviveInventory.openInventory(target);
            sender.sendMessage(ChatColor.GREEN + "Revive menu opened for " + target.getName() + ".");
        }
    }

    /**
     * Handles the "/heal" command.
     *
     * @param player The player executing the command.
     * @param group  The group data of the player.
     * @param args   The arguments provided by the user.
     */
    private void handleHealCommand(Player player, GroupData group, String[] args) {
        if (!group.getPermissions().contains(Permission.HEAL)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /heal <nombre del jugador>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(ChatColor.RED + "El jugador especificado no está en línea.");
            return;
        }

        playerController.healPlayer(target);
    }

    /**
     * Handles the "/mute" and "/unmute" commands.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
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

    /**
     * Handles the "/mute" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
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

    /**
     * Handles the "/endersee" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
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

    /**
     * Handles the "/inventorysee" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
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

    /**
     * Validates the group of the player.
     *
     * @param player     The player executing the command.
     * @param playerData The player data of the player.
     * @return true if the group is valid, false otherwise.
     */
    private boolean validateGroup(Player player, PlayerData playerData) {
        GroupData group = GroupDatabase.getGroupData(playerData.getGroup());
        if (group == null) {
            player.sendMessage(ChatColor.RED + "No tienes un grupo asignado.");
            return false;
        }
        return true;
    }

    /**
     * Handles the "/reportar" command.
     *
     * @param player The player executing the command.
     */
    private void handleMakeReport(Player player) {
        reportInventory.openInventory(player);
    }

    /**
     * Handles the "/reportes" command.
     *
     * @param player The player executing the command.
     * @param group  The group data of the player.
     */
    private void handleShowReports(Player player, GroupData group) {
        if (group.getPermissions().contains(Permission.REPORTS)) {
            reportsInventory.openInventory(player);
        } else {
            sendNoPermissionMessage(player);
        }
    }

    /**
     * Handles the "/reload" command.
     *
     * @param player The player executing the command.
     * @param group  The group data of the player.
     */
    private void handleReloadConfig(Player player, GroupData group) {
        if (group.getPermissions().contains(Permission.RELOAD)) {
            reloadConfig(player);
        } else {
            sendNoPermissionMessage(player);
        }
    }

    /**
     * Handles the "/tp" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
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
                    Message.sendMessage(player, "Te has teletransportado a " + args[1], ChatColor.GREEN);
                } else {
                    Message.sendMessage(player, "El jugador " + args[1] + " no está conectado.", ChatColor.RED);
                }
            }
            case 4 -> {
                // Teletransportarse a coordenadas
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    if (playerController.teleportToCoordinates(player, x, y, z)) {
                        Message.sendMessage(player, "Te has teletransportado a las coordenadas: " + x + ", " + y + ", " + z, ChatColor.GREEN);
                    }
                } catch (NumberFormatException e) {
                    Message.sendMessage(player, "Por favor, introduce coordenadas válidas.", ChatColor.RED);
                }
            }
            case 3 -> {
                // Teletransportar a un jugador a otro
                if (playerController.teleportPlayerToPlayer(args[1], args[2])) {
                    Message.sendMessage(player, "Has teletransportado a " + args[1] + " a " + args[2], ChatColor.GREEN);
                } else {
                    Message.sendMessage(player, "No se pudo completar el teletransporte.", ChatColor.RED);
                }
            }
            default -> Message.sendMessage(player, """
                Uso incorrecto del comando. Formatos válidos:
                /tp <jugador>
                /tp <x> <y> <z>
                /tp <jugador_origen> <jugador_destino>
                """, ChatColor.RED);
        }
    }

    /**
     * Handles the "/banshee" command.
     *
     * @param player The player executing the command.
     * @param group  The group data of the player.
     */
    private void handleBansheeCommand(Player player, GroupData group) {
        if (!group.getPermissions().contains(Permission.BANSHEE)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (playerController.isBansheeActive(player)) {
            playerController.deactivateBanshee(player);
            Message.sendMessage(player, "Banshee desactivado.", ChatColor.GREEN);
        } else {
            playerController.activateBanshee(player);
            Message.sendMessage(player, "Banshee activado.", ChatColor.GREEN);
        }
    }

    /**
     * Handles the "/añadirusuarioagrupo" and "/quitarusuariodegrupo" commands.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleAddUserToGroupCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP) && !player.isOp()) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 3) {
            Message.sendMessage(player, "Uso: /añadirusuarioagrupo <usuario> <grupo>", ChatColor.RED);
            return;
        }

        PlayerEditDatabase.addPlayerToGroup(args[1], args[2]);
        Message.sendMessage(player, "Usuario añadido al grupo correctamente.", ChatColor.GREEN);
    }

    /**
     * Handles the "/quitarusuariodegrupo" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleRemoveUserFromGroupCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.GROUP) && !player.isOp()) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /quitarusuariodegrupo <usuario>", ChatColor.RED);
            return;
        }

        PlayerEditDatabase.addPlayerToGroup(args[1], "default");
        Message.sendMessage(player, "Usuario eliminado del grupo correctamente.", ChatColor.GREEN);
    }

    /**
     * Handles the "/back" command.
     *
     * @param player The player executing the command.
     * @param group  The group data of the player.
     */
    private void handleBackCommand(Player player, GroupData group) {
        if (!group.getPermissions().contains(Permission.TELEPORT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (playerController.teleportBack(player)) {
            Message.sendMessage(player, "Te has teletransportado al punto anterior.", ChatColor.GREEN);
        } else {
            Message.sendMessage(player, "No hay un punto anterior al que teletransportarse.", ChatColor.RED);
        }
    }

    /**
     * Handles the "/setdia" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleSetDayCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.DAYS)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /setdia <número de días>", ChatColor.RED);
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
            Message.sendMessage(player, "Se ha establecido el día a " + days + " correctamente.", ChatColor.GREEN);
        } catch (NumberFormatException e) {
            Message.sendMessage(player, "Por favor, introduce un número válido de días.", ChatColor.RED);
        }
    }

    /**
     * Handles the "/añadirbannedword" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleAddBannedWordCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.CHAT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /añadirbannedword <palabra>", ChatColor.RED);
            return;
        }

        String word = args[1];
        BannedWordsDatabase.addBannedWord(word);
        Message.sendMessage(player, "La palabra " + word + " ha sido añadida a la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    /**
     * Handles the "/quitarbannedword" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleRemoveBannedWordCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.CHAT)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /quitarbannedword <palabra>", ChatColor.RED);
            return;
        }

        String word = args[1];
        BannedWordsDatabase.removeBannedWord(word);
        Message.sendMessage(player, "La palabra " + word + " ha sido eliminada de la lista de palabras prohibidas.", ChatColor.GREEN);
    }

    /**
     * Handles the "/quitarban" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     * @param group  The group data of the player.
     */
    private void handleUnbanPlayerCommand(Player player, String[] args, GroupData group) {
        if (!group.getPermissions().contains(Permission.BAN)) {
            sendNoPermissionMessage(player);
            return;
        }

        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /quitarban <usuario>", ChatColor.RED);
            return;
        }

        String targetPlayer = args[1];
        if (PlayerEditDatabase.isPlayerBanned(targetPlayer)) {
            PlayerEditDatabase.UnbanPlayer(targetPlayer);
            Message.sendMessage(player, "El jugador " + targetPlayer + " ha sido desbaneado.", ChatColor.GREEN);
        } else {
            Message.sendMessage(player, "El jugador " + targetPlayer + " no está baneado.", ChatColor.RED);
        }
    }

    /**
     * Handles the "/addvidas" command.
     *
     * @param sender The player executing the command.
     * @param args   The arguments provided by the user.
     */
    private void handleAddVidasCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /deathscape addvidas <jugador> <vidas>");
            return;
        }

        String targetName = args[1];
        int vidas;
        try {
            vidas = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "El número de vidas debe ser un entero.");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "El jugador no existe o nunca se ha conectado.");
            return;
        }

        if (PlayerEditDatabase.isPlayerBanned(target.getName())) {
            playerController.setLivesToPlayer(target, 0);
            PlayerEditDatabase.UnbanPlayer(target.getName());
            sender.sendMessage(ChatColor.GREEN + "Jugador " + target.getName() + " ha sido desbaneado con 1 vida.");
        } else {
            playerController.addLivesToPlayer(target, vidas);
            sender.sendMessage(ChatColor.GREEN + "Se han añadido " + vidas + " vidas a " + target.getName() + ".");
        }
    }

    /**
     * Handles the "/removevidas" command.
     *
     * @param sender The player executing the command.
     * @param args   The arguments provided by the user.
     */
    private void handleQuitarVidasCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Uso: /deathscape quitarvidas <jugador> <vidas>");
            return;
        }

        String targetName = args[1];
        int vidas;
        try {
            vidas = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "El número de vidas debe ser un entero.");
            return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "El jugador debe estar en línea para quitarle vidas.");
            return;
        }

        playerController.removeLivesFromPlayer(target, vidas);
        sender.sendMessage(ChatColor.GREEN + "Se han quitado " + vidas + " vidas a " + target.getName() + ".");
    }

    /**
     * Handles the "/vidas" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     */
    private void handleVidasCommand(Player player, String[] args) {
        // Si se pasan argumentos, el uso es incorrecto (de momento solo permitir /vidas sin más)
        if (args.length != 1) {
            Message.sendMessage(player, "Uso correcto: /deathscape vidas", ChatColor.RED);
            return;
        }

        // Obtenemos las vidas del jugador
        int vidas = playerController.getLivesOfPlayer(player);

        // Mostramos las vidas al jugador
        player.sendMessage(ChatColor.GOLD + "Te quedan " + ChatColor.RED + vidas + ChatColor.GOLD + " vidas.");
    }


    /**
     * Handles the "/deathscape" command.
     *
     * @param player The player executing the command.
     */
    private void showHelp(Player player) {
        // Obtener datos del grupo del jugador
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            Message.sendMessage(player, "No se pudo cargar tu información de grupo.", ChatColor.RED);
            return;
        }

        GroupData groupData = GroupDatabase.getGroupData(playerData.getGroup());
        if (groupData == null) {
            Message.sendMessage(player, "No tienes un grupo asignado.", ChatColor.RED);
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
        commands.put("leaderboard", "Muestra la tabla de clasificación de los jugadores.");
        commands.put("puntos", "Muestra los puntos del jugador.");
        commands.put("vidas", "Muestra las vidas del jugador.");
        commands.put("alijo", "Permite abrir un inventario que se mantiene durante temporadas.");
        commands.put("menu", "Abre el menú principal del servidor de una forma más visual");

        // Comandos adicionales según permisos
        if (groupPermissions.contains("group") || player.isOp()) {
            commands.put("añadirUsuarioAGrupo", "Añade un usuario a un grupo.");
            commands.put("quitarUsuarioDeGrupo", "Quita a un usuario de un grupo.");
            commands.put("inventorysee", "Permite ver el inventario de otro jugador.");
            commands.put("endersee", "Permite ver el Ender Chest de otro jugador.");
            commands.put("addvidas", "Permite añadir vidas a otro jugador.");
            commands.put("removevidas", "Permite quitar vidas a otro jugador.");
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

    /**
     * Handles the "/deathscape info" command.
     *
     * @param player The player executing the command.
     */
    private void showInfo(Player player) {
        String infoMessage = "DeathScape V" + plugin.getDescription().getVersion() + " por " + plugin.getDescription().getAuthors();
        Message.sendMessage(player, infoMessage, ChatColor.GREEN);
    }

    /** Handles the "/discord" command.
     *
     * @param player The player executing the command.
     */
    private void showDiscord(Player player) {
        Message.sendMessage(player, "Discord: " + DISCORD_URL, ChatColor.BLUE);
    }

    /**
     * Handles the "/deathscape tiempolluvia" command.
     *
     * @param player The player executing the command.
     */
    private void showStormTime(Player player) {
        int rainTimePending = plugin.getServerData().getStormPendingTime();
        Message.sendMessage(player, "Tiempo de lluvia pendiente: " + rainTimePending + " minutos.", ChatColor.GREEN);
    }

    /**
     * Handles the "/deathscape dia" command.
     *
     * @param player The player executing the command.
     */
    private void showDay(Player player) {
        Message.sendMessage(player, "El día actual es: " + plugin.getServerData().getServerDays(), ChatColor.GREEN);
    }

    /**
     * Handles the "/deathscape tiempojugado" command.
     *
     * @param player The player executing the command.
     */
    private void showPlayTime(Player player) {
        playerController.savePlayerData(player);
        String tiempoJugado = Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())).getTimePlayed();
        Message.sendMessage(player, "Has jugado un total de: " + tiempoJugado, ChatColor.GREEN);
    }

    /**
     * Reloads the plugin configuration.
     *
     * @param player The player executing the command.
     */
    private void reloadConfig(Player player) {
        if (MainConfigManager.getInstance().reloadConfig()) {
            Message.ConfigLoadedOK(player);
        } else {
            Message.ConfigLoadedError(player);
        }
    }

    /**
     * Sends an invalid command message to the player.
     *
     * @param player The player executing the command.
     */
    private void sendInvalidCommandMessage(Player player) {
        Message.sendMessage(player, INVALID_COMMAND_MESSAGE, ChatColor.RED);
    }

    /**
     * Sends a no permission message to the player.
     *
     * @param player The player executing the command.
     */
    private void sendNoPermissionMessage(Player player) {
        Message.sendMessage(player, "No tienes permiso para usar este comando.", ChatColor.RED);
    }

    /**
     * Handles the "/dificultad" command.
     *
     * @param player The player executing the command.
     * @param args   The arguments provided by the user.
     */
    private void handleDifficultyCommand(Player player, String[] args) {
        if (args.length < 2) {
            Message.sendMessage(player, "Uso: /dificultad <día>", ChatColor.RED);
            return;
        }

        // Convertir el argumento del día solicitado a un entero
        int requestedDay;
        try {
            requestedDay = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            Message.sendMessage(player, "El día ingresado no es válido. Por favor, ingrese un número.", ChatColor.RED);
            return;
        }

        int currentDay = plugin.getServerData().getServerDays();

        // Comparar el día solicitado con el día actual
        if (requestedDay > currentDay) {
            Message.sendMessage(player, "No puedes solicitar información para un día superior al actual (" + currentDay + ").", ChatColor.RED);
            return;
        }

        // Clave esperada en el archivo difficulties.yml
        String dayKey = "day_" + requestedDay;
        FileConfiguration difficultiesConfig = plugin.getDifficultiesConfig(); // Usamos el método del plugin

        // Validar si la sección "dificultades_info" existe
        ConfigurationSection difficultiesSection = difficultiesConfig.getConfigurationSection("dificultades_info");
        if (difficultiesSection == null) {
            Message.sendMessage(player, "No se encontró la sección 'dificultades_info' en el archivo de configuración.", ChatColor.RED);
            return;
        }

        // Validar si la clave específica del día existe
        if (!difficultiesSection.contains(dayKey)) {
            Message.sendMessage(player, "No se encontró información para el día: " + requestedDay, ChatColor.RED);
            return;
        }

        // Obtener la lista de líneas para la clave del día
        List<String> infoLines = difficultiesSection.getStringList(dayKey);
        if (infoLines.isEmpty()) {
            Message.sendMessage(player, "No hay detalles disponibles para el día: " + requestedDay, ChatColor.RED);
            return;
        }

        // Enviar los detalles al jugador
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_GREEN + "Dificultades" + ChatColor.DARK_GREEN + " para el día " + requestedDay + ":");
        for (String line : infoLines) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    private void handleLeaderboardServer(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Uso correcto: /ds leaderboard");
            return;
        }

        List<PlayerData> leaderboard = PlayerDatabase.getLeaderboard();

        if (leaderboard.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No hay datos disponibles para mostrar el leaderboard.");
            return;
        }

        sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "===== Leaderboard de Puntos =====");
        for (int i = 0; i < leaderboard.size(); i++) {
            PlayerData data = leaderboard.get(i);
            sender.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". "
                    + ChatColor.AQUA + data.getName()
                    + ChatColor.GRAY + " - "
                    + ChatColor.GREEN + data.getPoints() + " puntos");
        }
        LeaderboardExporter.exportLeaderboardAsHtml("plugins/DeathScape/index.html");
    }

    private void handleLeaderboard(Player player, String[] args) {
        int page = 1; // Página por defecto
        int itemsPerPage = 10;

        // Validación de argumentos
        if (args.length != 2) {
            Message.sendMessage(player, "Uso correcto: /ds leaderboard [página]", ChatColor.RED);
            return;
        }

        // Parsear número de página si se pasa como argumento
        else {
            try {
                page = Integer.parseInt(args[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                Message.sendMessage(player, "El número de página debe ser un número válido.", ChatColor.RED);
                return;
            }
        }

        List<PlayerData> leaderboard = PlayerDatabase.getLeaderboard();

        if (leaderboard.isEmpty()) {
            Message.sendMessage(player, "No hay datos disponibles para mostrar el leaderboard.", ChatColor.RED);
            return;
        }

        int totalPages = (int) Math.ceil(leaderboard.size() / (double) itemsPerPage);
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, leaderboard.size());

        player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "===== Leaderboard de Puntos (Página " + page + "/" + totalPages + ") =====");

        for (int i = startIndex; i < endIndex; i++) {
            PlayerData data = leaderboard.get(i);
            player.sendMessage(ChatColor.YELLOW + "" + (i + 1) + ". "
                    + ChatColor.AQUA + data.getName()
                    + ChatColor.GRAY + " - "
                    + ChatColor.GREEN + data.getPoints() + " puntos");
        }

        // Mensaje para navegar entre páginas
        if (page < totalPages) {
            player.sendMessage(ChatColor.GRAY + "Usa " + ChatColor.YELLOW + "/leaderboard " + (page + 1)
                    + ChatColor.GRAY + " para ver la siguiente página.");
        }
    }

    private void handlePoints(Player player, String[] args) {
        // Verificamos que no se pasen argumentos (solo /points es válido)
        if (args.length != 1) {
            Message.sendMessage(player, "Uso correcto: /puntos", ChatColor.RED);
            return;
        }

        PlayerData data = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        if (data == null) {
            Message.sendMessage(player, "No se encontraron datos para tu jugador.", ChatColor.RED);
            return;
        }

        // Mostramos los puntos
        int points = data.getPoints();
        player.sendMessage(ChatColor.GOLD + "Tienes " + ChatColor.GREEN + points + ChatColor.GOLD + " puntos.");
    }

    /**
     * Maneja el comando /alijo.
     * Sintaxis correcta: /alijo
     *
     * @param player Jugador que ejecuta el comando
     * @param args   Argumentos (se espera solo el nombre del comando)
     */
    private void handlePlayerStash(Player player, String[] args) {
        if (args.length != 1) {
            Message.sendMessage(player, "Uso correcto: /deathscape alijo", ChatColor.RED);
            return;
        }

        Inventory inv = Bukkit.createInventory(player, 9, ChatColor.DARK_PURPLE + "Tu Alijo");

        // Item para bloquear posiciones
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Slot bloqueado");
        pane.setItemMeta(meta);

        // Bloquear todos los slots por defecto
        for (int i = 4; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }

        // Slots centrales usables (0-3)
        int[] usable = {0, 1, 2, 3};
        List<ItemStack> stash = PlayerStashDatabase.getStash(player.getName());

        for (int i = 0; i < usable.length; i++) {
            if (i < stash.size() && stash.get(i) != null) {
                inv.setItem(usable[i], stash.get(i));
            }
        }

        // Abrir el inventario
        player.openInventory(inv);
    }
}
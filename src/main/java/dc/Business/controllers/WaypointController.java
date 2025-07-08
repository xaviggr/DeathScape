package dc.Business.controllers;

import dc.Business.player.PlayerWaypoint;
import dc.DeathScape;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import dc.Persistence.waypoints.WaypointDatabase;
import java.util.*;

public class WaypointController {

    public static final Map<UUID, Map<String, Location>> waypoints = new HashMap<>();
    private final DeathScape plugin;
    private Map<UUID, List<PlayerWaypoint>> activeWaypoints = new HashMap<>();
    private final Map<UUID, Boolean> waypointTracking = new HashMap<>();

    public WaypointController(DeathScape plugin) {
        // Cargar todos los waypoints guardados desde la base de datos
        for (UUID playerUUID : WaypointDatabase.getAllPlayersWithWaypoints()) {
            Map<String, Location> loadedWaypoints = WaypointDatabase.getPlayerWaypoints(playerUUID);
            waypoints.put(playerUUID, loadedWaypoints);
        }
        this.plugin = plugin;
    }

    public void handleWaypointCommand(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint <add | remove | list | coords | compartir | copiarcompartido | activar> [nombre] [in <x> <y> <z>]");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> {
                if (args.length > 2 && args[2].equalsIgnoreCase("in")) {
                    addWaypointAtCoords(player, args);
                } else {
                    addWaypoint(player, args);
                }
            }
            case "remove" -> removeWaypoint(player, args);
            case "list" -> listWaypoints(player);
            case "coords" -> showWaypointCoords(player, args);
            case "compartir" -> compartirWaypoint(player, args);
            case "copiarcompartido" -> handleCopySharedWaypoint(player, args);
            case "renombrar" -> renameWaypoint(player, args);
            default -> player.sendMessage(ChatColor.RED + "Comando desconocido. Usa /ds waypoint <add | remove | list | coords | compartir | copiarcompartido | activar> [nombre] [in <x> <y> <z>]");
        }
    }



    private void addWaypoint(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint add <nombre>");
            return;
        }

        String name = args[1];
        UUID uuid = player.getUniqueId();

        if (waypoints.containsKey(uuid) && waypoints.get(uuid).containsKey(name)) {
            player.sendMessage(ChatColor.RED + "Ya tienes un waypoint con el nombre " + ChatColor.YELLOW + name + ChatColor.RED + ".");
            return;
        }

        Location loc = player.getLocation();
        waypoints.computeIfAbsent(uuid, k -> new HashMap<>()).put(name, loc);
        WaypointDatabase.addWaypoint(uuid, name, loc); // Guardar en base de datos

        player.sendMessage(ChatColor.GREEN + "Waypoint " + ChatColor.YELLOW + name + ChatColor.GREEN +
                " añadido en las coordenadas " + ChatColor.AQUA + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void addWaypointAtCoords(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint add <nombre> in <x> <y> <z>");
            return;
        }

        String name = args[1];
        try {
            double x = Double.parseDouble(args[3]);
            double y = Double.parseDouble(args[4]);
            double z = Double.parseDouble(args[5]);
            Location loc = new Location(player.getWorld(), x, y, z);

            UUID uuid = player.getUniqueId();

            waypoints.computeIfAbsent(uuid, k -> new HashMap<>()).put(name, loc);
            WaypointDatabase.addWaypoint(uuid, name, loc); // Guardar en base de datos

            player.sendMessage(ChatColor.GREEN + "Waypoint " + ChatColor.YELLOW + name + ChatColor.GREEN +
                    " añadido en las coordenadas " + ChatColor.AQUA + x + ", " + y + ", " + z);
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Las coordenadas deben ser números válidos.");
        }
    }

    private void removeWaypoint(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint remove <nombre>");
            return;
        }

        String name = args[1];
        UUID uuid = player.getUniqueId();

        Map<String, Location> playerWaypoints = waypoints.get(uuid);
        if (playerWaypoints == null || !playerWaypoints.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "No tienes un waypoint con ese nombre.");
            return;
        }

        playerWaypoints.remove(name);
        WaypointDatabase.removeWaypoint(uuid, name); // Eliminar de base de datos

        player.sendMessage(ChatColor.YELLOW + "Waypoint " + ChatColor.GOLD + name + ChatColor.YELLOW + " eliminado.");
    }

    private void listWaypoints(Player player) {
        Map<String, Location> playerWaypoints = waypoints.get(player.getUniqueId());
        if (playerWaypoints == null || playerWaypoints.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No tienes waypoints.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Tus waypoints:");
        for (String name : playerWaypoints.keySet()) {
            player.sendMessage(ChatColor.YELLOW + "- " + name);
        }
    }

    private void showWaypointCoords(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint coords <nombre>");
            return;
        }

        String name = args[1];
        Map<String, Location> playerWaypoints = waypoints.get(player.getUniqueId());
        if (playerWaypoints == null || !playerWaypoints.containsKey(name)) {
            player.sendMessage(ChatColor.RED + "No tienes un waypoint con ese nombre.");
            return;
        }

        Location loc = playerWaypoints.get(name);
        player.sendMessage(ChatColor.GOLD + "Coordenadas de " + name + ": "
                + ChatColor.AQUA + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
    }

    private void compartirWaypoint(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint compartir <nombre> [to <jugador>]");
            return;
        }

        String waypointName = args[1];
        Map<String, Location> playerWaypoints = waypoints.get(player.getUniqueId());

        if (playerWaypoints == null || !playerWaypoints.containsKey(waypointName)) {
            player.sendMessage(ChatColor.RED + "No tienes un waypoint con ese nombre.");
            return;
        }

        // Construimos el mensaje con el botón "Copiar"
        TextComponent message = new TextComponent(ChatColor.GOLD + player.getName() + " compartió un waypoint: ");
        TextComponent nameComponent = new TextComponent(ChatColor.AQUA + waypointName + " ");
        TextComponent copyComponent = new TextComponent(ChatColor.GREEN + "[Copiar]");

        String commandToRun = "/ds waypoint copiarcompartido " + waypointName + " " + player.getName();

        copyComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToRun));
        copyComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click para añadir este waypoint").create()));

        message.addExtra(nameComponent);
        message.addExtra(copyComponent);

        // Si el comando incluye "to <jugador>", enviamos privado
        if (args.length >= 4 && args[2].equalsIgnoreCase("to")) {
            String targetPlayerName = args[3];
            Player targetPlayer = Bukkit.getPlayerExact(targetPlayerName);

            if (targetPlayer == null || !targetPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "El jugador '" + targetPlayerName + "' no está conectado.");
                return;
            }

            targetPlayer.spigot().sendMessage(message);
            player.sendMessage(ChatColor.GREEN + "Waypoint '" + waypointName + "' compartido con " + targetPlayerName + ".");
        } else {
            // Si no hay "to", se comparte públicamente (broadcast)
            Bukkit.spigot().broadcast(message);
        }
    }



    public static Map<String, Location> getWaypointsForPlayer(Player player) {
        return waypoints.getOrDefault(player.getUniqueId(), new HashMap<>());
    }

    public void loadPlayerWaypoints(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> loadedWaypoints = WaypointDatabase.getPlayerWaypoints(uuid);
        waypoints.put(uuid, loadedWaypoints != null ? loadedWaypoints : new HashMap<>());
    }

    public void renameWaypoint(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /ds waypoint renombrar <nombreActual> <nuevoNombre>");
            return;
        }

        UUID uuid = player.getUniqueId();
        Map<String, Location> playerWaypoints = waypoints.get(uuid);

        if (playerWaypoints == null || !playerWaypoints.containsKey(args[1])) {
            player.sendMessage(ChatColor.RED + "No tienes un waypoint llamado '" + args[1] + "'.");
            return;
        }

        if (playerWaypoints.containsKey(args[2])) {
            player.sendMessage(ChatColor.RED + "Ya tienes un waypoint llamado '" + args[2] + "'. Usa otro nombre.");
            return;
        }

        String currentName = args[1];
        String newName = args[2];
        Location location = playerWaypoints.remove(currentName);
        playerWaypoints.put(newName, location);

        WaypointDatabase.removeWaypoint(uuid, currentName);
        WaypointDatabase.addWaypoint(uuid, newName, location);

        player.sendMessage(ChatColor.GREEN + "Waypoint '" + ChatColor.YELLOW + currentName + ChatColor.GREEN + "' renombrado a '" + ChatColor.YELLOW + newName + ChatColor.GREEN + "'.");
    }


    public void handleCopySharedWaypoint(Player receiver, String[] args) {
        if (args.length < 3) {
            receiver.sendMessage(ChatColor.RED + "Uso: /ds waypoint copiarcompartido <nombre> <jugador>");
            return;
        }

        String waypointName = args[1];
        String senderPlayerName = args[2];

        OfflinePlayer sender = Bukkit.getOfflinePlayer(senderPlayerName);
        if (!sender.hasPlayedBefore()) {
            receiver.sendMessage(ChatColor.RED + "El jugador " + senderPlayerName + " no existe o nunca se ha conectado.");
            return;
        }

        UUID senderUUID = sender.getUniqueId();
        Map<String, Location> senderWaypoints = WaypointController.waypoints.get(senderUUID);
        if (senderWaypoints == null || !senderWaypoints.containsKey(waypointName)) {
            receiver.sendMessage(ChatColor.RED + "El jugador " + senderPlayerName + " no tiene un waypoint llamado '" + waypointName + "'.");
            return;
        }

        Location loc = senderWaypoints.get(waypointName);
        UUID receiverUUID = receiver.getUniqueId();

        Map<String, Location> receiverWaypoints = WaypointController.waypoints.computeIfAbsent(receiverUUID, k -> new HashMap<>());
        if (receiverWaypoints.containsKey(waypointName)) {
            receiver.sendMessage(ChatColor.RED + "Ya tienes un waypoint llamado '" + waypointName + "'.");
            return;
        }

        receiverWaypoints.put(waypointName, loc);
        WaypointDatabase.addWaypoint(receiverUUID, waypointName, loc); // Guardar en base de datos

        receiver.sendMessage(ChatColor.GREEN + "Waypoint '" + ChatColor.YELLOW + waypointName + ChatColor.GREEN + "' copiado de " + ChatColor.AQUA + senderPlayerName);
    }

}

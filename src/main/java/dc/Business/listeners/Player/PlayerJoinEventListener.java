package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.groups.GroupData;
import dc.DeathScape;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Business.player.PlayerData;
import dc.Persistence.config.MainConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class PlayerJoinEventListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    public PlayerJoinEventListener(DeathScape plugin, PlayerController playerController) {
        this.playerController = playerController;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        GroupData groupData = null;
        World queueWorld = Bukkit.getWorld("world_minecraft_spawn");
        World overworld = Bukkit.getWorld("world");

        // Cargar o crear los datos del jugador
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {

            Location spawnLocation = (overworld != null) ? overworld.getSpawnLocation() : new Location(null, 0, 0, 0);
            String spawnCoordinates = spawnLocation.getX() + "," + spawnLocation.getY() + "," + spawnLocation.getZ();

            playerData = new PlayerData(player.getName(), false, 0, hostAddress, "0", player.getUniqueId(), "0", "0", spawnCoordinates, 0, "default");

            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error loading your data, please contact an administrator.");
                return; // Detener el flujo
            }
        } else {
            // Obtener datos del grupo
            groupData = GroupDatabase.getGroupData(playerData.getGroup());
            if (groupData == null) {
                player.kickPlayer(ChatColor.RED + "You don't have a group assigned, please contact an administrator.");
                return; // Detener el flujo
            }

            // Validar si el jugador está marcado como muerto
            if (playerData.isDead() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "You are dead, please contact an administrator.");
                return; // Detener el flujo
            }

            // Validar si la IP ha cambiado
            if (!Objects.equals(playerData.getHostAddress(), hostAddress)
                    && MainConfigManager.getInstance().isKickIfIpChanged()
                    && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Your IP has changed, please contact an administrator.");
                return; // Detener el flujo
            }
        }

        String prefix = null;
        if (groupData != null) {
            prefix = groupData.getPrefix();
        }
        if (prefix == null) {
            prefix = "&b[Warrior]";
        }
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        event.setJoinMessage(prefix + " " + ChatColor.YELLOW + player.getName() + " ha entrado al mundo DeathScape");

        if (player.isOnline()) {
            plugin.time_of_connection.put(player.getName(), System.currentTimeMillis());
        }
        playerController.setPlayerRank(player, prefix);
        playerController.setUpTabList(player);

        // Teletransportar al jugador al mundo de espera
        if (queueWorld != null) {
            Location queueSpawn = new Location(queueWorld, 0, 1, 0); // Coordenadas de la isla de espera
            player.teleport(queueSpawn);
            player.sendTitle(
                    ChatColor.AQUA + "🌐 Bienvenido a " + ChatColor.BOLD + "DeathScape",
                    ChatColor.YELLOW + "Estamos verificando el estado del servidor...",
                    20, 100, 20
            );

            // Espera de 3 segundos para comprobar el estado de la cola.
            new BukkitRunnable() {
                @Override
                public void run() {
                    playerController.addPlayerToServer(player);

                    // Inicia el bucle de comprobación de la posición en la cola.
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            int position = playerController.getQueuePosition(player); // Obtener la posición del jugador en la cola
                            if (position > 0) {
                                player.sendTitle(
                                        ChatColor.AQUA + "🌐 DeathScape",
                                        ChatColor.YELLOW + "Estás en la posición " + ChatColor.AQUA + position + ChatColor.YELLOW + " de la cola.",
                                        20, 100, 20
                                );
                            } else {
                                player.sendTitle(
                                        ChatColor.AQUA + "🌐 DeathScape",
                                        ChatColor.YELLOW + "¡Es tu turno! ¡Bienvenido a DeathScape!",
                                        20, 100, 20
                                );
                                cancel(); // Detener el bucle una vez que el jugador es procesado.
                            }
                        }
                    }.runTaskTimer(plugin, 0, 800); // Repetir cada 40 segundos
                }
            }.runTaskLater(plugin, 100); // 5 segundos = 100 ticks

        } else {
            player.kickPlayer(ChatColor.RED + "The queue world is not available. Please contact an administrator.");
            return; // Detener el flujo
        }
    }
}

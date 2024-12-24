package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.groups.GroupData;
import dc.DeathScape;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Business.player.PlayerData;
import dc.Persistence.config.MainConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class PlayerJoinEventListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    public PlayerJoinEventListener(DeathScape plugin, PlayerController playerController) {
        this.plugin = plugin;
        this.playerController = playerController;
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        // Cargar o inicializar datos del jugador
        PlayerData playerData = loadOrCreatePlayerData(player, hostAddress);
        if (playerData == null) return; // Si ocurre un error, el jugador ya fue expulsado.

        // Configurar el grupo del jugador
        GroupData groupData = configurePlayerGroup(player, playerData);
        if (groupData == null) return; // Si ocurre un error, el jugador ya fue expulsado.

        // Configurar mensajes de bienvenida y prefijo
        setupPlayerEnvironment(event, player, groupData);

        // Teletransportar y manejar vida en mundos específicos
        handlePlayerTeleportAndHealth(player, playerData);
    }

    private PlayerData loadOrCreatePlayerData(Player player, String hostAddress) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        if (playerData == null) {
            // Crear nuevos datos de jugador
            playerData = new PlayerData(player.getName(), false, 0, hostAddress, "0", player.getUniqueId(), "0", "0",
                    "0,0,0", 0, "default");

            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error loading your data, please contact an administrator.");
                return null;
            }
        } else {
            // Validar estado del jugador
            if (!validatePlayerState(player, playerData, hostAddress)) return null;
        }

        return playerData;
    }

    private boolean validatePlayerState(Player player, PlayerData playerData, String hostAddress) {
        if (playerData.isDead() && !player.isOp()) {
            player.kickPlayer(ChatColor.RED + "You are dead, please contact an administrator.");
            return false;
        }

        if (!Objects.equals(playerData.getHostAddress(), hostAddress)
                && MainConfigManager.getInstance().isKickIfIpChanged()
                && !player.isOp()) {
            player.kickPlayer(ChatColor.RED + "Your IP has changed, please contact an administrator.");
            return false;
        }

        return true;
    }

    private GroupData configurePlayerGroup(Player player, PlayerData playerData) {
        GroupData groupData = GroupDatabase.getGroupData(playerData.getGroup());

        if (groupData == null) {
            player.kickPlayer(ChatColor.RED + "You don't have a group assigned, please contact an administrator.");
            return null;
        }

        return groupData;
    }

    private void setupPlayerEnvironment(PlayerJoinEvent event, Player player, GroupData groupData) {
        String prefix = (groupData != null) ? groupData.getPrefix() : "&b[Warrior]";
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        event.setJoinMessage(prefix + " " + ChatColor.YELLOW + player.getName() + " ha entrado al mundo DeathScape");

        if (player.isOnline()) {
            plugin.time_of_connection.put(player.getName(), System.currentTimeMillis());
        }

        playerController.setPlayerRank(player, prefix);
        playerController.setUpTabList(player);
    }

    private void handlePlayerTeleportAndHealth(Player player, PlayerData playerData) {
        World spawnWorld = Bukkit.getWorld("world_minecraft_spawn");
        World overworld = Bukkit.getWorld("world");

        if (spawnWorld != null) {
            // Teletransportar al spawn y establecer vida máxima
            player.teleport(new Location(spawnWorld, 0, 1, 0));
            player.setHealth(player.getMaxHealth());
            player.sendTitle(
                    ChatColor.AQUA + "🌐 Bienvenido a " + ChatColor.BOLD + "DeathScape",
                    ChatColor.YELLOW + "Estamos verificando el estado del servidor...",
                    20, 100, 20
            );

            // Manejo de cola y verificación
            handleQueue(player);
        } else {
            player.kickPlayer(ChatColor.RED + "The spawn world is not available. Please contact an administrator.");
        }
    }

    private void handleQueue(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerController.addPlayerToServer(player);

                // Bucle para verificar posición en la cola
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int position = playerController.getQueuePosition(player);

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
                            cancel(); // Detener bucle
                        }
                    }
                }.runTaskTimer(plugin, 0, 800); // Repetir cada 40 segundos
            }
        }.runTaskLater(plugin, 100); // Ejecutar después de 5 segundos (100 ticks)
    }
}
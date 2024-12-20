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

import java.util.Objects;

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
        String hostAddress = getPlayerHostAddress(player);
        if (hostAddress == null) {
            kickPlayer(player, "Error retrieving your IP address, please contact an administrator.");
            return;
        }

        PlayerData playerData = loadOrCreatePlayerData(player, hostAddress);
        if (playerData == null) return; // Player already kicked for errors

        GroupData groupData = validatePlayerGroup(player, playerData);
        if (groupData == null) return; // Player already kicked for errors

        if (!validatePlayerState(player, playerData, hostAddress)) return; // Player already kicked

        setupPlayerJoinMessage(event, player, groupData);
        setupPlayerSession(player);

        if (!teleportPlayerToQueue(player)) {
            kickPlayer(player, "The queue world is not available. Please contact an administrator.");
        }
    }

    private String getPlayerHostAddress(Player player) {
        return player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
    }

    private PlayerData loadOrCreatePlayerData(Player player, String hostAddress) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        if (playerData == null) {
            Location spawnLocation = getSpawnLocation();
            String spawnCoordinates = formatCoordinates(spawnLocation);

            playerData = new PlayerData(player.getName(), false, 0, hostAddress, "0", player.getUniqueId(), "0", "0", spawnCoordinates, 0, "default");

            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                kickPlayer(player, "Error loading your data, please contact an administrator.");
                return null;
            }
        }
        return playerData;
    }

    private Location getSpawnLocation() {
        World overworld = Bukkit.getWorld("world");
        return overworld != null ? overworld.getSpawnLocation() : new Location(null, 0, 0, 0);
    }

    private String formatCoordinates(Location location) {
        return location.getX() + "," + location.getY() + "," + location.getZ();
    }

    private GroupData validatePlayerGroup(Player player, PlayerData playerData) {
        GroupData groupData = GroupDatabase.getGroupData(playerData.getGroup());

        if (groupData == null) {
            kickPlayer(player, "You don't have a group assigned, please contact an administrator.");
        }
        return groupData;
    }

    private boolean validatePlayerState(Player player, PlayerData playerData, String hostAddress) {
        if (playerData.isDead() && !player.isOp()) {
            kickPlayer(player, "You are dead, please contact an administrator.");
            return false;
        }

        if (!Objects.equals(playerData.getHostAddress(), hostAddress) &&
                MainConfigManager.getInstance().isKickIfIpChanged() && !player.isOp()) {
            kickPlayer(player, "Your IP has changed, please contact an administrator.");
            return false;
        }

        return true;
    }

    private void setupPlayerJoinMessage(PlayerJoinEvent event, Player player, GroupData groupData) {
        String prefix = groupData != null && groupData.getPrefix() != null ? groupData.getPrefix() : "&b[Warrior]";
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        event.setJoinMessage(prefix + " " + ChatColor.YELLOW + player.getName() + " ha entrado al mundo DeathScape");
    }

    private void setupPlayerSession(Player player) {
        if (player.isOnline()) {
            plugin.time_of_connection.put(player.getName(), System.currentTimeMillis());
        }
        playerController.setPlayerRank(player, "&b[Warrior]");
        playerController.setUpTabList(player);
    }

    private boolean teleportPlayerToQueue(Player player) {
        World queueWorld = Bukkit.getWorld("world_minecraft_spawn");

        if (queueWorld == null) return false;

        Location queueSpawn = new Location(queueWorld, 0, 1, 0);
        player.teleport(queueSpawn);
        player.sendTitle(
                ChatColor.AQUA + "\uD83C\uDF10 Bienvenido a " + ChatColor.BOLD + "DeathScape",
                ChatColor.YELLOW + "Estamos verificando el estado del servidor...",
                20, 100, 20
        );

        new BukkitRunnable() {
            @Override
            public void run() {
                playerController.addPlayerToServer(player);

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
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 800);
            }
        }.runTaskLater(plugin, 100);

        return true;
    }

    private void kickPlayer(Player player, String message) {
        player.kickPlayer(ChatColor.RED + message);
    }
}
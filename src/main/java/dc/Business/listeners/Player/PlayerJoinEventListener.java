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

/**
 * Listener for handling player join events. Manages player data loading, group configuration,
 * queue handling, and teleportation to the spawn world.
 */
public class PlayerJoinEventListener implements Listener {

    private final PlayerController playerController;
    private final DeathScape plugin;

    /**
     * Constructs a `PlayerJoinEventListener` with the required plugin and player controller.
     *
     * @param plugin           The instance of the DeathScape plugin.
     * @param playerController The controller managing player-related functionality.
     */
    public PlayerJoinEventListener(DeathScape plugin, PlayerController playerController) {
        this.plugin = plugin;
        this.playerController = playerController;
    }

    /**
     * Handles the `PlayerJoinEvent` triggered when a player joins the server.
     * Manages player data initialization, group setup, queue handling, and teleportation.
     *
     * @param event The `PlayerJoinEvent` triggered when a player joins.
     */
    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        // Load or initialize player data
        PlayerData playerData = loadOrCreatePlayerData(player, hostAddress);
        if (playerData == null) return;

        // Configure the player's group
        GroupData groupData = configurePlayerGroup(player, playerData);
        if (groupData == null) return;

        // Setup player environment
        setupPlayerEnvironment(event, player, groupData);

        // Teleport and handle health in specific worlds
        handlePlayerTeleportAndHealth(player);

        // Manage invisibility during queue
        playerController.addInvisiblePlayer(player);
        playerController.applyInvisibilityToPlayer(player);
    }

    /**
     * Loads existing player data or creates new data if the player is joining for the first time.
     *
     * @param player      The player joining the server.
     * @param hostAddress The host address of the player's connection.
     * @return The `PlayerData` object for the player, or null if loading fails.
     */
    private PlayerData loadOrCreatePlayerData(Player player, String hostAddress) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        if (playerData == null) {
            // Initialize new player data
            World world = Bukkit.getWorld("world");
            assert world != null;
            Location location = world.getSpawnLocation();
            String spawnPos = location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();

            playerData = new PlayerData(
                    player.getName(), false, 0, hostAddress, "0", player.getUniqueId(), "0", "0",
                    spawnPos, 0, "default"
            );

            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error loading your data, please contact an administrator.");
                return null;
            }
        } else {
            if (!validatePlayerState(player, playerData, hostAddress)) return null;
        }

        return playerData;
    }

    /**
     * Validates the state of the player, ensuring they are not dead or joining with a different IP address.
     *
     * @param player      The player joining the server.
     * @param playerData  The `PlayerData` object for the player.
     * @param hostAddress The host address of the player's connection.
     * @return True if the player's state is valid, false otherwise.
     */
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

    /**
     * Configures the player's group by retrieving group data from the database.
     *
     * @param player     The player joining the server.
     * @param playerData The `PlayerData` object for the player.
     * @return The `GroupData` object for the player's group, or null if the group is not found.
     */
    private GroupData configurePlayerGroup(Player player, PlayerData playerData) {
        GroupData groupData = GroupDatabase.getGroupData(playerData.getGroup());

        if (groupData == null) {
            player.kickPlayer(ChatColor.RED + "You don't have a group assigned, please contact an administrator.");
            return null;
        }

        return groupData;
    }

    /**
     * Sets up the player's environment, including setting up prefixes, tab lists, and welcome messages.
     *
     * @param event     The `PlayerJoinEvent` triggered when the player joins.
     * @param player    The player joining the server.
     * @param groupData The `GroupData` object for the player's group.
     */
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

    /**
     * Teleports the player to the spawn world and restores their health.
     *
     * @param player The player joining the server.
     */
    private void handlePlayerTeleportAndHealth(Player player) {
        World spawnWorld = Bukkit.getWorld("world_minecraft_spawn");

        if (spawnWorld != null) {
            player.teleport(new Location(spawnWorld, 0, 1, 0));
            player.setHealth(player.getMaxHealth());
            player.sendTitle(
                    ChatColor.AQUA + "🌐 Bienvenido a " + ChatColor.BOLD + "DeathScape",
                    ChatColor.YELLOW + "Estamos verificando el estado del servidor...",
                    20, 100, 20
            );

            handleQueue(player);
        } else {
            player.kickPlayer(ChatColor.RED + "The spawn world is not available. Please contact an administrator.");
        }
    }

    /**
     * Handles the queue system for players joining the server.
     *
     * @param player The player joining the server.
     */
    private void handleQueue(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                playerController.addPlayerToServer(player);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int position = playerController.getQueuePosition(player);

                        if (position > 0 && !hasPriority(player)) {
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
                            playerController.removeInvisiblePlayer(player);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0, 800);
            }
        }.runTaskLater(plugin, 100);
    }


    /**
     * Checks if the player belongs to a priority group (Tier1, Tier2, or Tier3).
     *
     * @param player The player to check.
     * @return True if the player has a priority tier permission, false otherwise.
     */
    private boolean hasPriority(Player player) {
        String group = playerController.getGroupFromPlayer(player);
        return group != null && (
                group.equalsIgnoreCase("tier1") ||
                        group.equalsIgnoreCase("tier2") ||
                        group.equalsIgnoreCase("tier3") ||
                        group.equalsIgnoreCase("owner")
        );
    }
}
package dc.Persistence.player;

import dc.Business.groups.GroupData;
import dc.Business.player.PlayerData;
import dc.Persistence.groups.GroupDatabase;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Utility class for managing player data and editing player-related information in the database.
 */
public class PlayerEditDatabase {

    /**
     * Marks the specified player as dead and increments their death count.
     *
     * @param playerDeath The player to mark as dead.
     */
    public static void setPlayerAsDeath(Player playerDeath) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(playerDeath.getName());
        if (playerData == null) {
            playerDeath.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerData.setDeaths(playerData.getDeaths() + 1);
            playerData.setDead(true);
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Sets the ban date and time for a player.
     *
     * @param player The player to update.
     */
    public static void setPlayerBanDate(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            player.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerData.setBanTime();
            playerData.setBanDate();
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Updates the total playtime for a player by adding the specified time.
     *
     * @param player       The player to update.
     * @param new_seconds  Seconds to add.
     * @param new_minutes  Minutes to add.
     * @param new_hours    Hours to add.
     */
    public static void setPlayerTimePlayed(Player player, int new_seconds, int new_minutes, int new_hours) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            player.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            String[] parts = playerData.getTimePlayed().split("\\s+");
            int seconds = 0, minutes = 0, hours = 0;

            for (String part : parts) {
                int values = Integer.parseInt(part.substring(0, part.length() - 1));
                if (part.endsWith("s")) {
                    seconds = values;
                } else if (part.endsWith("m")) {
                    minutes = values;
                } else if (part.endsWith("h")) {
                    hours = values;
                }
            }

            seconds += new_seconds;
            if (seconds >= 60) {
                new_minutes += seconds / 60;
                seconds %= 60;
            }
            minutes += new_minutes;
            if (minutes >= 60) {
                new_hours += minutes / 60;
                minutes %= 60;
            }
            hours += new_hours;

            playerData.setTimePlayed(hours + "h " + minutes + "m " + seconds + "s");
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Saves the current coordinates and dimension of a player.
     *
     * @param player The player to update.
     */
    public static void setPlayerCoords(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            player.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerData.setCoords(player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ());
            playerData.setDimension(player.getWorld().getName());
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Unbans a player by resetting their death and ban status.
     *
     * @param playerName The name of the player to unban.
     */
    public static void UnbanPlayer(String playerName) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(playerName);
        if (playerData != null) {
            playerData.setDead(false);
            playerData.setBanDate("0");
            playerData.setBantime("0");
            playerData.setHealth(20);
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Adds a player to a specific group.
     *
     * @param player The name of the player to add.
     * @param group  The name of the group.
     * @return True if the operation is successful, false otherwise.
     */
    public static boolean addPlayerToGroup(String player, String group) {
        GroupData groupData = GroupDatabase.getGroupData(group.toLowerCase());
        if (groupData == null) {
            return false;
        }
        if (groupData.getPlayers().contains(player)) {
            return false;
        }
        removePlayerFromGroup(player);
        groupData.addPlayer(player);
        GroupDatabase.addGroupData(groupData);
        PlayerDatabase.setPlayerGroup(player, group);
        return true;
    }

    /**
     * Removes a player from their current group.
     *
     * @param player The name of the player to remove.
     */
    public static void removePlayerFromGroup(String player) {
        GroupData groupData = GroupDatabase.getGroupData(Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player)).getGroup());
        assert groupData != null;
        if (!groupData.getPlayers().contains(player)) {
            return;
        }
        groupData.removePlayer(player);
        GroupDatabase.addGroupData(groupData);
    }

    /**
     * Retrieves the saved location of a player.
     *
     * @param name The name of the player.
     * @return A {@link Location} object representing the player's saved location, or null if not found.
     */
    public static Location getPlayerLocation(String name) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(name);
        if (playerData == null) {
            return null;
        }
        String[] coords = playerData.getCoords().split(",");
        World world = playerData.getDimension();
        return new Location(world, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
    }

    /**
     * Updates the health value of a player in the database.
     *
     * @param player The player whose health is to be updated.
     */
    public static void setPlayerHealth(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            player.kickPlayer("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerData.setHealth(player.getHealth());
            PlayerDatabase.addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Checks if a player is currently banned.
     *
     * @param targetPlayer The name of the player to check.
     * @return True if the player is banned, false otherwise.
     */
    public static boolean isPlayerBanned(String targetPlayer) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(targetPlayer);
        if (playerData == null) {
            return false;
        }
        return playerData.isDead();
    }
}
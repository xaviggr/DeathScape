package dc.Business.groups;

import com.google.gson.Gson;

import java.util.List;

/**
 * Represents the data structure for a group, including its name, prefix,
 * permissions, and the list of players belonging to the group.
 */
public class GroupData {

    private final String name; // Name of the group
    private final String prefix; // Prefix associated with the group
    private final List<Permission> permissions; // List of permissions assigned to the group
    private final List<String> players; // List of player names belonging to the group

    /**
     * Constructs a GroupData object with the specified group information.
     *
     * @param groupName   The name of the group.
     * @param prefix      The prefix associated with the group.
     * @param permissions The list of permissions assigned to the group.
     * @param players     The list of players belonging to the group.
     */
    public GroupData(String groupName, String prefix, List<Permission> permissions, List<String> players) {
        this.name = groupName;
        this.prefix = prefix;
        this.permissions = permissions;
        this.players = players;
    }

    /**
     * Converts the group data to a JSON string using Gson.
     *
     * @return The JSON representation of the group data.
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    /**
     * Gets the name of the group.
     *
     * @return The group's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the prefix associated with the group.
     *
     * @return The group's prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the list of permissions assigned to the group.
     *
     * @return The list of permissions.
     */
    public List<Permission> getPermissions() {
        return permissions;
    }

    /**
     * Gets the list of players belonging to the group.
     *
     * @return The list of player names in the group.
     */
    public List<String> getPlayers() {
        return players;
    }

    /**
     * Adds a player to the group.
     *
     * @param playerData The name of the player to add.
     */
    public void addPlayer(String playerData) {
        players.add(playerData);
    }

    /**
     * Removes a player from the group by their name.
     *
     * @param name The name of the player to remove.
     */
    public void removePlayer(String name) {
        players.remove(name);
    }
}

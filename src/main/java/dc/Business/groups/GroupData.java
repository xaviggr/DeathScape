package dc.Business.groups;

import com.google.gson.Gson;

import java.util.List;

public class GroupData {

    private final String name;
    private final String prefix;
    private final List<Permission> permissions;
    private final List<String> players;

    public GroupData(String groupName, String prefix, List<Permission> permissions, List<String> players) {
        this.name = groupName;
        this.prefix = prefix;
        this.permissions = permissions;
        this.players = players;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void addPlayer(String playerData) {
        players.add(playerData);
    }

    public void removePlayer(String name) {
        players.remove(name);
    }
}

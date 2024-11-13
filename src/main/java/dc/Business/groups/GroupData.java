package dc.Business.groups;

import com.google.gson.Gson;
import dc.Persistence.player.PlayerData;

import java.util.List;

public class GroupData {

    private final String name;
    private final String prefix;
    private final List<Permission> permissions;
    private final List<PlayerData> players;

    public GroupData(String groupName, String prefix, List<Permission> permissions, List<PlayerData> players) {
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

    public List<PlayerData> getPlayers() {
        return players;
    }
}

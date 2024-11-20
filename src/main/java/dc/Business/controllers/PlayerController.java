package dc.Business.controllers;

import dc.Business.groups.GroupData;
import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.Business.player.PlayerTabList;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.groups.GroupDatabase;
import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.entity.Player;

public class PlayerController {

    private final PlayerDeath playerDeath;
    private final PlayerTabList playerTabList;

    public PlayerController(DeathScape plugin) {
        PlayerBan playerBan = new PlayerBan();
        this.playerTabList = new PlayerTabList(plugin,this);
        this.playerDeath = new PlayerDeath(plugin, playerBan);
    }

    public void setPlayerAsDead(Player player) {
        playerDeath.Dead(player);
        int points = MainConfigManager.getInstance().getPointsToReduceOnDeath() * -1;
        addPointsToPlayer(player, points);
    }

    public boolean setGroupToPlayer(Player player, String group) {
        return PlayerEditDatabase.addPlayerToGroup(player.getName(), group);
    }

    public boolean removeGroupFromPlayer(Player player) {
        return PlayerEditDatabase.removePlayerFromGroup(player.getName());
    }


    public void addPointToPlayer(Player player) {
        addPointsToPlayer(player, 1);
    }

    public void addPointsToPlayer(Player player, int points) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        assert playerData != null;
        playerData.setPoints(Math.max(playerData.getPoints() + points, 0));
        PlayerDatabase.addPlayerDataToDatabase(playerData);
    }

    public void setUpTabList(Player player) {
        playerTabList.startAnimation(player);
    }

    public String getGroupFromPlayer(Player player) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        assert playerData != null;
        return playerData.getGroup();
    }
}

package dc.Business.controllers;

import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.Business.player.PlayerTabList;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import org.bukkit.entity.Player;

import javax.naming.Name;

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
        int points = MainConfigManager.getInstance().getPoints_to_reduce_on_death() * -1;
        addPointsToPlayer(player, points);
    }

    public void setGroupToPlayer(Player player, String group) {
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());

        assert playerData != null;
        playerData.setGroup(group);
        PlayerDatabase.addPlayerDataToDatabase(playerData);
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

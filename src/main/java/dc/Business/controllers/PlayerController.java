package dc.Business.controllers;

import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.DeathScape;
import dc.Persistence.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import org.bukkit.entity.Player;

public class PlayerController {

    private final DeathScape plugin;
    private final PlayerDeath playerDeath;

    public PlayerController(DeathScape plugin) {
        this.plugin = plugin;
        PlayerBan playerBan = new PlayerBan();
        this.playerDeath = new PlayerDeath(plugin, playerBan);
    }

    public void setPlayerAsDead(Player player) {
        playerDeath.Dead(player);
        int points = plugin.getMainConfigManager().getPoints_to_reduce_on_death() * -1;
        addPointsToPlayer(player, points);
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
}

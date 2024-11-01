package dc.Business.controllers;

import dc.Business.player.PlayerBan;
import dc.Business.player.PlayerDeath;
import dc.DeathScape;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class PlayerController {

    private DeathScape plugin;
    private PlayerBan playerBan;
    private PlayerDeath playerDeath;

    public PlayerController(DeathScape plugin) {
        this.plugin = plugin;
        this.playerBan = new PlayerBan();
        this.playerDeath = new PlayerDeath(plugin,playerBan);
    }

    public void setPlayerAsDead(Player player) {
        playerDeath.Dead(player);
    }
}

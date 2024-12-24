package dc.Business.player;

import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Date;

public class PlayerBan {
    public void Ban(Player player, String Reason, Date date) {
        PlayerEditDatabase.setPlayerBanDate(player);
    }
}

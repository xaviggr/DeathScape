package dc.player;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;

public class PlayerBan {
    public static void Ban(Player player, String Reason, Date date) {
        Bukkit.getBanList (BanList.Type.NAME).addBan (player.getName (), Reason, date, "console");
    }
}

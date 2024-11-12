package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Color;

import java.util.List;

public class ReviveInventory extends PlayersInventory {

    public ReviveInventory() {
        super("Revivir Jugador");
    }

    @Override
    protected List<String> getPlayersList() {
        return PlayerDatabase.getDeadPlayers();
    }

    @Override
    protected String getPlayerDisplayName(String playerName) {
        return "Revivir a " + playerName;
    }

    @Override
    protected void onPlayerSelected(Player reviver, String playerName) {
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
        PlayerEditDatabase.UnbanPlayer(playerName);
        Message.sendMessageAllPlayers(playerName + " ha sido revivido por " + reviver.getName(), ChatColor.GREEN);
    }
}

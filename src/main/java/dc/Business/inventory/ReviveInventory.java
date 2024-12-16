package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.Color;

import java.util.List;
import java.util.Objects;

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

        // Reducir la vida máxima del que revive
        double currentMaxHealth = Objects.requireNonNull(reviver.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
        if (currentMaxHealth > 2) { // Asegurarse de que no baje de 1 corazón
            Objects.requireNonNull(reviver.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(currentMaxHealth - 2);
            Message.enviarMensajeColorido(reviver, "Has perdido un corazón por revivir a " + playerName + ".", ChatColor.RED);
        } else {
            Message.enviarMensajeColorido(reviver, "¡No puedes perder más corazones!", ChatColor.RED);
        }
    }
}

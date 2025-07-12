package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

/**
 * A specialized inventory system for reviving players. Allows players to select
 * a dead player from a list and revive them at the cost of their own health.
 */
public class ReviveInventory extends PlayersInventory {

    /**
     * Constructs a ReviveInventory with the title "Revivir Jugador".
     */
    public ReviveInventory() {
        super("Revivir Jugador");
    }

    /**
     * Retrieves the list of dead players from the database to be displayed in the inventory.
     *
     * @return A list of dead player names.
     */
    @Override
    protected List<String> getPlayersList() {
        return PlayerDatabase.getDeadPlayers();
    }

    /**
     * Sets the display name for each dead player in the inventory.
     * The name format will be "Revivir a [playerName]".
     *
     * @param playerName The name of the dead player.
     * @return The display name for the player's item in the inventory.
     */
    @Override
    protected String getPlayerDisplayName(String playerName) {
        return "Revivir a " + playerName;
    }

    /**
     * Handles the selection of a dead player to revive. Unbans the selected player
     * and notifies all players. The reviver loses 1 heart (2 health points) as a cost
     * for reviving the selected player.
     *
     * @param reviver    The player who is reviving another player.
     * @param playerName The name of the player being revived.
     */
    @Override
    protected void onPlayerSelected(Player reviver, String playerName) {
        // Verifica si el jugador existe (ha estado antes en el servidor)
        if (!Bukkit.getOfflinePlayer(playerName).hasPlayedBefore()) {
            Message.sendMessage(reviver, "El jugador '" + playerName + "' no existe o nunca ha entrado al servidor.", ChatColor.RED);
            return;
        }

        // Unban the player
        Bukkit.getBanList(org.bukkit.BanList.Type.NAME).pardon(playerName);
        PlayerEditDatabase.UnbanPlayer(playerName);

        // Notify all players about the revival
        Message.sendMessageAllPlayers(playerName + " ha sido revivido por " + reviver.getName(), ChatColor.GREEN);

        // Reduce the reviver's maximum health
        double currentMaxHealth = Objects.requireNonNull(reviver.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
        if (currentMaxHealth > 2) { // Ensure the player doesn't drop below 1 heart
            reviver.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(currentMaxHealth - 2);
            Message.sendMessage(reviver, "Has perdido un corazón por revivir a " + playerName + ".", ChatColor.RED);
        } else {
            Message.sendMessage(reviver, "¡No puedes perder más corazones!", ChatColor.RED);
        }
    }
}

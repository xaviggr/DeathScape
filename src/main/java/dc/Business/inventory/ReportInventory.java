package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A specialized inventory for reporting players. Allows a player to select another player
 * from a list and provide a reason for reporting them.
 */
public class ReportInventory extends PlayersInventory {

    /**
     * A map that stores the pending reports. The key is the reporter, and the value is the
     * name of the player being reported.
     */
    private static final Map<Player, String> pendingReports = new HashMap<>();

    /**
     * Constructs a ReportInventory with the title "Reportar Jugador".
     */
    public ReportInventory() {
        super("Reportar Jugador");
    }

    /**
     * Retrieves the list of all players from the PlayerDatabase to be displayed in the inventory.
     *
     * @return A list of player names.
     */
    @Override
    protected List<String> getPlayersList() {
        return PlayerDatabase.getAllPlayers();
    }

    /**
     * Sets the display name for each player in the inventory.
     * The name format will be "Reportar a [playerName]".
     *
     * @param playerName The name of the player.
     * @return The display name for the player's item in the inventory.
     */
    @Override
    protected String getPlayerDisplayName(String playerName) {
        return "Reportar a " + playerName;
    }

    /**
     * Handles the selection of a player to report. Adds the reporter and the reported player
     * to the pending reports map and prompts the reporter to provide a reason in chat.
     *
     * @param reporter   The player who is reporting.
     * @param playerName The name of the player being reported.
     */
    @Override
    protected void onPlayerSelected(Player reporter, String playerName) {
        pendingReports.put(reporter, playerName); // Add the reporter and reported player to the map
        reporter.closeInventory();
        reporter.sendMessage("Escribe en el chat el motivo del reporte para " + playerName);
    }

    /**
     * Gets the map of pending reports. This map contains players who are in the process of
     * reporting someone, along with the name of the player they are reporting.
     *
     * @return A map of pending reports.
     */
    public static Map<Player, String> getPendingReports() {
        return pendingReports;
    }
}

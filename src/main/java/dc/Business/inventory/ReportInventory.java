package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import dc.utils.Message;
import org.bukkit.Color;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportInventory extends PlayersInventory {

    private static final Map<Player, String> pendingReports = new HashMap<>();

    public ReportInventory() {
        super("Reportar Jugador");
    }

    @Override
    protected List<String> getPlayersList() {
        return PlayerDatabase.getAllPlayers();
    }

    @Override
    protected String getPlayerDisplayName(String playerName) {
        return "Reportar a " + playerName;
    }

    @Override
    protected void onPlayerSelected(Player reporter, String playerName) {
        // Agrega al jugador que va a reportar y al jugador que va a ser reportado en el mapa
        pendingReports.put(reporter, playerName);
        reporter.closeInventory();
        reporter.sendMessage("Escribe en el chat el motivo del reporte para " + playerName);
    }

    public static Map<Player, String> getPendingReports() {
        return pendingReports;
    }
}

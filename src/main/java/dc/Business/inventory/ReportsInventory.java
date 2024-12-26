package dc.Business.inventory;

import dc.Persistence.chat.ReportsDatabase;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A specialized inventory system for viewing reports. Displays all reports stored in the database
 * and allows players to interact with individual reports to view their details.
 */
public class ReportsInventory extends InventorySystem {

    /**
     * Constructs a ReportsInventory with the title "Reportes".
     */
    public ReportsInventory() {
        super("Reportes");
    }

    /**
     * Populates the inventory with items representing individual reports.
     * Each item displays the report's details such as the reporter, reported player, reason, and timestamp.
     */
    @Override
    protected void generateInventory() {
        items.clear();

        // Retrieve reports from the database
        JsonArray reports = ReportsDatabase.getReports();

        // Create items for each report
        for (int i = 0; i < reports.size(); i++) {
            JsonObject report = reports.get(i).getAsJsonObject();

            String reporter = report.get("reporter").getAsString();
            String reported = report.get("reported").getAsString();
            String reason = report.get("reason").getAsString();
            String timestamp = report.get("timestamp").getAsString();

            // Create an item for the report
            ItemStack reportItem = new ItemStack(Material.PAPER);
            ItemMeta meta = reportItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("Reporte de " + reporter);

                List<String> lore = new ArrayList<>();
                lore.add("Reportado: " + reported);
                lore.add("Razón: " + reason);
                lore.add("Fecha: " + timestamp);

                meta.setLore(lore);
                reportItem.setItemMeta(meta);
            }

            // Add the item to the inventory list
            items.add(reportItem);
        }
    }

    /**
     * Handles interactions with report items in the inventory. When a player clicks on a report,
     * its details are displayed in the chat.
     *
     * @param player The player interacting with the inventory.
     * @param item   The item clicked in the inventory.
     */
    @Override
    protected void onItemClicked(Player player, ItemStack item) {
        if (item.getType() == Material.PAPER && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String reportTitle = meta.getDisplayName();
                if (reportTitle.startsWith("Reporte de ")) {
                    // Extract report details from the item's lore
                    List<String> lore = meta.getLore();
                    if (lore != null && lore.size() >= 3) {
                        String reporter = reportTitle.replace("Reporte de ", "");
                        String reported = lore.get(0).replace("Reportado: ", "");
                        String reason = lore.get(1).replace("Razón: ", "");
                        String timestamp = lore.get(2).replace("Fecha: ", "");

                        // Display report details to the player
                        player.sendMessage("Detalles del reporte:");
                        player.sendMessage("Reportado por: " + reporter);
                        player.sendMessage("Reportado: " + reported);
                        player.sendMessage("Razón: " + reason);
                        player.sendMessage("Fecha: " + timestamp);
                    }
                }
            }
        }
    }
}

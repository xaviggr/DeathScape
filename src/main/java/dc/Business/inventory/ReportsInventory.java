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

public class ReportsInventory extends InventorySystem {

    public ReportsInventory() {
        super("Reportes");
    }

    @Override
    protected void generateInventory() {
        items.clear();

        // Obtener los reportes desde la base de datos
        JsonArray reports = ReportsDatabase.getReports();

        // Crear los items para cada reporte
        for (int i = 0; i < reports.size(); i++) {
            JsonObject report = reports.get(i).getAsJsonObject();

            String reporter = report.get("reporter").getAsString();
            String reported = report.get("reported").getAsString();
            String reason = report.get("reason").getAsString();
            String timestamp = report.get("timestamp").getAsString();

            // Crear un item para el reporte
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

            // Agregar el item a la lista
            items.add(reportItem);
        }
    }

    @Override
    protected void onItemClicked(Player player, ItemStack item) {
        if (item.getType() == Material.PAPER && item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String reportTitle = meta.getDisplayName();
                if (reportTitle.startsWith("Reporte de ")) {
                    // Extraer la información del reporte desde el nombre o el lore
                    List<String> lore = meta.getLore();
                    if (lore != null && lore.size() >= 3) {
                        String reporter = reportTitle.replace("Reporte de ", "");
                        String reported = lore.get(0).replace("Reportado: ", "");
                        String reason = lore.get(1).replace("Razón: ", "");
                        String timestamp = lore.get(2).replace("Fecha: ", "");

                        // Mostrar los detalles del reporte al jugador
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

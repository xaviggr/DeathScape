package dc.Business.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class InventorySystem implements Listener {

    protected int currentPage;
    protected final int itemsPerPage = 27;
    protected List<ItemStack> items;
    protected String title;

    public InventorySystem(String title) {
        this.title = title;
        this.currentPage = 0;
        this.items = new ArrayList<>();
    }

    public void openInventory(Player player) {
        generateInventory();

        // Calcular el número total de páginas
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        // Limitar currentPage para que esté dentro del rango válido
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 36, title + " - Página " + (currentPage + 1));

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        // Llenar el inventario con los items de la página actual
        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, items.get(i));
        }

        // Agregar botones de navegación
        if (currentPage > 0) {
            inventory.setItem(27, createNavigationItem("Página Anterior"));
        }
        if (currentPage < totalPages - 1) {
            inventory.setItem(35, createNavigationItem("Página Siguiente"));
        }

        player.openInventory(inventory);
    }

    private ItemStack createNavigationItem(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(title)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        // Manejar botones de navegación
        if (itemName.equals("Página Anterior")) {
            // Solo decrementa si no estamos en la primera página
            if (currentPage > 0) {
                currentPage--;
            }
            openInventory(player);
        } else if (itemName.equals("Página Siguiente")) {
            // Solo incrementa si no estamos en la última página
            int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
            }
            openInventory(player);
        } else {
            onItemClicked(player, clickedItem);
        }
    }

    protected abstract void onItemClicked(Player player, ItemStack item);
    protected abstract void generateInventory();
}
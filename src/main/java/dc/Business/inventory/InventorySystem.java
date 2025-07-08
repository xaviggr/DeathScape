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

/**
 * Abstract class to manage a paginated inventory system in a Bukkit/Spigot plugin.
 * Handles navigation between pages and provides hooks for custom inventory behavior.
 */
public abstract class InventorySystem implements Listener {

    protected int currentPage; // The current page being displayed
    protected final int itemsPerPage = 27; // Number of items per page
    protected List<ItemStack> items; // List of all items to display in the inventory
    protected String title; // Title of the inventory

    /**
     * Constructs an InventorySystem with a specified title.
     *
     * @param title The title of the inventory.
     */
    public InventorySystem(String title) {
        this.title = title;
        this.currentPage = 0;
        this.items = new ArrayList<>();
    }

    /**
     * Opens the inventory for the specified player and displays the current page.
     *
     * @param player The player to whom the inventory will be shown.
     */
    public void openInventory(Player player) {
        generateInventory();

        // Calculate the total number of pages
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);

        // Ensure currentPage is within valid bounds
        currentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 36, title + " - Página " + (currentPage + 1));

        int start = currentPage * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());

        // Populate the inventory with items from the current page
        for (int i = start; i < end; i++) {
            inventory.setItem(i - start, items.get(i));
        }

        // Add navigation buttons
        if (currentPage > 0) {
            inventory.setItem(27, createNavigationItem("Página Anterior"));
        }
        if (currentPage < totalPages - 1) {
            inventory.setItem(35, createNavigationItem("Página Siguiente"));
        }

        player.openInventory(inventory);
    }

    /**
     * Creates a navigation item (arrow) for switching between pages.
     *
     * @param name The display name of the navigation item.
     * @return An ItemStack representing the navigation item.
     */
    private ItemStack createNavigationItem(String name) {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Handles inventory click events, managing navigation and custom item interactions.
     *
     * @param event The InventoryClickEvent triggered by a player.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(title)) return;

        event.setCancelled(true); // Prevent default inventory behavior
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String itemName = Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName();

        // Handle navigation buttons
        if (itemName.equals("Página Anterior")) {
            if (currentPage > 0) {
                currentPage--;
            }
            openInventory(player);
        } else if (itemName.equals("Página Siguiente")) {
            int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
            }
            openInventory(player);
        } else {
            // Handle custom item click behavior
            onItemClicked(player, clickedItem);
            player.playSound(player.getLocation(), "ui.button.click", 1, 1);
        }
    }

    /**
     * Abstract method to define custom behavior when an item is clicked.
     *
     * @param player The player who clicked the item.
     * @param item   The item that was clicked.
     */
    protected abstract void onItemClicked(Player player, ItemStack item);

    /**
     * Abstract method to populate the inventory with items.
     * Must be implemented by subclasses to define the inventory content.
     */
    protected abstract void generateInventory();
}

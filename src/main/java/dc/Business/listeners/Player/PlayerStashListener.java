package dc.Business.listeners.Player;

import dc.Persistence.stash.PlayerStashDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerStashListener implements Listener {

    private static final String STASH_TITLE = "Tu Alijo";
    private static final String LAST_SEASON_STASH_TITLE = "Tu Alijo Anterior";

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        String title = ChatColor.stripColor(e.getView().getTitle());

        if (title.equalsIgnoreCase(STASH_TITLE)) {
            // STASH NORMAL
            int raw = e.getRawSlot();
            if (raw < 9 && !isUsable(raw, e.getView().getTopInventory())) {
                e.setCancelled(true);
                return;
            }

            // Bloquear shulkers con contenido y cristales tintados
            ItemStack cursor = e.getCursor();
            if (raw < 9 && isUsable(raw, e.getView().getTopInventory()) && cursor != null && cursor.getType() != Material.AIR) {
                if (isIllegalItem(cursor)) {
                    p.sendMessage(ChatColor.RED + "¡No puedes meter shulkers con objetos dentro al alijo!");
                    e.setCancelled(true);
                    return;
                }
            }

            if (e.isShiftClick() && raw >= 9) {
                boolean free = false;
                for (int i = 0; i < e.getView().getTopInventory().getSize(); i++) {
                    ItemStack it = e.getView().getTopInventory().getItem(i);
                    if (isUsable(i, e.getView().getTopInventory()) && (it == null || it.getType() == Material.AIR)) {
                        free = true;
                        break;
                    }
                }
                if (!free) e.setCancelled(true);
            }

        } else if (title.equalsIgnoreCase(LAST_SEASON_STASH_TITLE)) {
            // STASH ANTERIOR
            int rawSlot = e.getRawSlot();
            if (rawSlot >= e.getView().getTopInventory().getSize()) return;

            ItemStack cursor = e.getCursor();
            if (cursor != null && cursor.getType() != Material.AIR) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "¡No puedes meter objetos en el alijo anterior!");
                return;
            }
        }
    }

    @EventHandler
    public void onInvDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        String title = ChatColor.stripColor(e.getView().getTitle());

        if (title.equalsIgnoreCase(STASH_TITLE)) {
            // STASH NORMAL
            ItemStack item = e.getOldCursor();
            if (item == null || item.getType() == Material.AIR) return;
            if (!isIllegalItem(item)) return;

            for (int slot : e.getRawSlots()) {
                if (slot < 9 && isUsable(slot, e.getView().getTopInventory())) {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "¡No puedes meter shulkers con objetos dentro al alijo!");
                    break;
                }
            }

        } else if (title.equalsIgnoreCase(LAST_SEASON_STASH_TITLE)) {
            // STASH ANTERIOR
            for (int slot : e.getRawSlots()) {
                if (slot < e.getView().getTopInventory().getSize()) {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "¡No puedes meter objetos en el alijo anterior!");
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;

        String title = ChatColor.stripColor(e.getView().getTitle());

        if (title.equalsIgnoreCase(STASH_TITLE)) {
            // STASH NORMAL → guardar correctamente según slots usables (NO hardcodear a 4 slots)
            Inventory inv = e.getInventory();
            List<ItemStack> stash = new ArrayList<>();

            for (int i = 0; i < inv.getSize(); i++) {
                if (isUsable(i, inv)) {
                    ItemStack item = inv.getItem(i);
                    stash.add(item);
                }
            }

            PlayerStashDatabase.setStash(p.getName(), stash);
            p.sendMessage(ChatColor.GRAY + "Tu alijo ha sido guardado correctamente.");

        } else if (title.equalsIgnoreCase(LAST_SEASON_STASH_TITLE)) {
            // STASH ANTERIOR → no guardar nada
            p.sendMessage(ChatColor.GRAY + "Has cerrado el alijo de la temporada anterior.");
        }
    }

    private boolean isUsable(int slot, Inventory inv) {
        // Un slot usable es cualquier slot que NO contenga el cristal bloqueador
        ItemStack item = inv.getItem(slot);
        if (item == null) return true;
        return item.getType() != Material.GRAY_STAINED_GLASS_PANE;
    }

    private boolean isIllegalItem(ItemStack item) {
        Material type = item.getType();

        if (type == Material.TINTED_GLASS) return true;

        if (type.name().contains("SHULKER_BOX")) {
            if (item.hasItemMeta() && item.getItemMeta() instanceof BlockStateMeta meta) {
                if (meta.getBlockState() instanceof ShulkerBox box) {
                    for (ItemStack inside : box.getInventory().getContents()) {
                        if (inside != null && inside.getType() != Material.AIR) return true; // contiene algo
                    }
                }
            }
        }

        return false;
    }
}
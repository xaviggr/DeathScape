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

    private static final int[] USABLE = {0, 1, 2, 3};
    private static final String TITLE = ChatColor.DARK_PURPLE + "Tu Alijo";

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        int raw = e.getRawSlot();
        if (raw < 9 && !isUsable(raw)) {
            e.setCancelled(true);
            return;
        }

        // Bloquear shulkers con contenido y cristales tintados
        ItemStack cursor = e.getCursor();
        if (raw < 9 && isUsable(raw) && cursor != null && cursor.getType() != Material.AIR) {
            if (isIllegalItem(cursor)) {
                p.sendMessage(ChatColor.RED + "¡No puedes meter ese objeto en el alijo!");
                e.setCancelled(true);
                return;
            }
        }

        if (e.isShiftClick() && raw >= 9) {
            boolean free = false;
            for (int s : USABLE) {
                ItemStack it = e.getView().getTopInventory().getItem(s);
                if (it == null || it.getType() == Material.AIR) {
                    free = true;
                    break;
                }
            }
            if (!free) e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        Inventory inv = e.getInventory();
        List<ItemStack> stash = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ItemStack item = inv.getItem(USABLE[i]);
            stash.add(item);
        }

        PlayerStashDatabase.setStash(p.getName(), stash);
    }

    private boolean isUsable(int slot) {
        for (int s : USABLE) if (s == slot) return true;
        return false;
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

    @EventHandler
    public void onInvDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        ItemStack item = e.getOldCursor();
        if (item == null || item.getType() == Material.AIR) return;
        if (!isIllegalItem(item)) return;

        for (int slot : e.getRawSlots()) {
            if (slot < 9 && isUsable(slot)) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "¡No puedes arrastrar ese objeto al alijo!");
                break;
            }
        }
    }
}

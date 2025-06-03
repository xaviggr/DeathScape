package dc.Business.listeners.Player;

import dc.Persistence.stash.PlayerStashDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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

        // Bloquear shulkers y cristales tintados
        ItemStack cursor = e.getCursor();
        if (raw < 9 && isUsable(raw) && cursor != null && cursor.getType() != Material.AIR) {
            if (isIllegalItem(cursor.getType())) {
                p.sendMessage(ChatColor.RED + "¡No puedes meter ese objeto en el alijo!");
                e.setCancelled(true);
                return;
            }
        }

        // Bloquear shift-click si no hay espacio
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

    private boolean isIllegalItem(Material type) {
        // Todos los tipos de shulker + tinted glass
        return type.name().contains("SHULKER_BOX") || type == Material.TINTED_GLASS;
    }
}

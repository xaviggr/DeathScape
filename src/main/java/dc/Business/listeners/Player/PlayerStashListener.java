package dc.Business.listeners.Player;

import dc.Persistence.player.PlayerDatabase;
import dc.Business.player.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerStashListener implements Listener {

    private static final int[] USABLE = {0, 1, 2, 3};
    private static final String TITLE = ChatColor.DARK_PURPLE + "Tu Alijo";

    /* ---------- Bloquear clics en slots no permitidos ---------- */
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        int raw = e.getRawSlot();
        if (raw < 9 && !isUsable(raw)) {            // Solo 0-3 usables
            e.setCancelled(true);
            return;
        }

        // Evitar shift-click si no hay huecos usables libres
        if (e.isShiftClick() && raw >= 9) {
            boolean free = false;
            for (int s : USABLE) {
                ItemStack it = e.getView().getTopInventory().getItem(s);
                if (it == null || it.getType() == Material.AIR) { free = true; break; }
            }
            if (!free) e.setCancelled(true);
        }
    }

    /* ---------- Guardar al cerrar ---------- */
    @EventHandler
    public void onInvClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!e.getView().getTitle().equals(TITLE)) return;

        Inventory inv = e.getInventory();
        ItemStack[] stash = new ItemStack[4];
        for (int i = 0; i < 4; i++) stash[i] = inv.getItem(USABLE[i]);

        PlayerData data = PlayerDatabase.getPlayerDataFromDatabase(p.getName());
        if (data == null) return;

        data.setStash(stash);
        PlayerDatabase.addPlayerDataToDatabase(data); // persistir
    }

    private boolean isUsable(int slot) {
        for (int s : USABLE) if (s == slot) return true;
        return false;
    }
}

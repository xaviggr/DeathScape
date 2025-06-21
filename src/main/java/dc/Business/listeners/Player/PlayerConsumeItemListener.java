package dc.Business.listeners.Player;

import dc.DeathScape;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class PlayerConsumeItemListener implements Listener {

    private final DeathScape plugin;

    public PlayerConsumeItemListener(DeathScape plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;

        ItemMeta meta = event.getItem().getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "CustomItemType");

        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        String type = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (type == null) return;

        Player player = event.getPlayer();

        if (type.equalsIgnoreCase("JumpBoost")) {
            player.setVelocity(new Vector(0, 2.0, 0));
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
            player.sendMessage(ChatColor.AQUA + "¡La manzana te ha impulsado hacia el cielo!");
        }
    }
}

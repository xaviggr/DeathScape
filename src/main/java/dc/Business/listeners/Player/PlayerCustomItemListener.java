package dc.Business.listeners.Player;

import dc.DeathScape;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerCustomItemListener implements Listener {

    private final DeathScape plugin;
    private final Map<UUID, Long> dashCooldown = new HashMap<>();

    public PlayerCustomItemListener(DeathScape plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUseCustomItem(PlayerInteractEvent event) {
        // Solo click derecho
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        // 🔒 Filtra para que solo se dispare desde la mano principal
        if (event.getHand() != EquipmentSlot.HAND) return;

        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;

        ItemMeta meta = event.getItem().getItemMeta();
        if (meta == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "CustomItemType");
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) return;

        String type = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (type == null) return;

        Player player = event.getPlayer();
        long now = System.currentTimeMillis();

        if (type.equalsIgnoreCase("dash")) {
            int cooldownTicks = 60; // 3 segundos (20 ticks por segundo)

            Long lastUsed = dashCooldown.get(player.getUniqueId());
            if (lastUsed != null && now - lastUsed < cooldownTicks * 50L) {
                player.sendMessage("§cDebes esperar antes de usar Dash de nuevo.");
                return;
            }

            dashCooldown.put(player.getUniqueId(), now);

            // ⏳ Establecer cooldown visual (gris en el icono del ítem)
            player.setCooldown(event.getItem().getType(), cooldownTicks);

            // Movimiento tipo dash
            Vector dash = player.getLocation().getDirection().normalize().multiply(2.0);
            dash.setY(0.3);
            player.setVelocity(dash);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        }
    }
}

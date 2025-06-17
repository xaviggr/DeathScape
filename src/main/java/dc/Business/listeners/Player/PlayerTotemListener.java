package dc.Business.listeners.Player;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;

public class PlayerTotemListener implements Listener {

    private final Plugin plugin;

    public PlayerTotemListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return; // Solo nos interesa el jugador
        }

        Player player = (Player) event.getEntity();

        // Revisamos la mano principal y la secundaria
        ItemStack[] hands = {
                player.getInventory().getItemInMainHand(),
                player.getInventory().getItemInOffHand()
        };

        for (ItemStack item : hands) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                NamespacedKey key = new NamespacedKey(plugin, "TotemType");

                if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    String totemType = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

                    // 🔸 Aquí aplicas tu efecto personalizado:
                    if (totemType.equals("Jump")) {
                        player.setVelocity(new Vector(0, 2, 0)); // ~20 bloques de altura
                        player.sendMessage(ChatColor.GREEN + "¡Tu Totem de Salto te ha salvado!");
                    }
                    else if (totemType.equals("Explosion")) {
                        player.getWorld().createExplosion(player.getLocation(), 8.0f, true, true);
                        player.sendMessage(ChatColor.RED + "¡Tu Totem de Explosión se ha activado!");
                    }

                    // 🔸 Opcional: Cancelar el efecto vanilla del totem
                    // event.setCancelled(true);

                    return; // Ya hemos encontrado un totem válido, no seguimos
                }
            }
        }
    }
}

package dc.Business.listeners.Player;

import dc.DeathScape;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.UUID;

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

        else if (type.equalsIgnoreCase("Invisibilidad")) {
            player.sendMessage(ChatColor.DARK_PURPLE + "¡Te has vuelto invisible!");
            player.setInvisible(true);

            // Opcional: efecto visual
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1f, 1.5f);

            // Evita detección por mobs
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 200, 0, false, false, false));

            // Quitar invisibilidad tras 10 segundos
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setInvisible(false);
                player.sendMessage(ChatColor.RED + "Has vuelto a ser visible.");
            }, 200L); // 200 ticks = 10 segundos
        }

         /**else if (type.equalsIgnoreCase("Beleño")) {
            UUID uuid = player.getUniqueId();

            player.sendMessage(ChatColor.RED + "¡El beleño negro arde en tu interior!");

            // Aplica +100% daño cuerpo a cuerpo
            player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 200, 1, false, false, false));

            // Marca al jugador como con doble daño activo (solo a mobs)
            plugin.getMetadataManager().markPlayerDoubleDamage(uuid, true);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getMetadataManager().markPlayerDoubleDamage(uuid, false);
                player.sendMessage(ChatColor.GRAY + "El efecto del beleño ha desaparecido.");
            }, 200L); // 10 segundos
        }**/

        else if (type.equalsIgnoreCase("NectarCaida")) {
            UUID uuid = player.getUniqueId();

            plugin.getMetadataManager().markPlayerNoFallDamage(uuid, true);
            player.sendMessage(ChatColor.GOLD + "¡El néctar del guardián te protege de caídas durante 1 minuto!");
            player.playSound(player.getLocation(), Sound.ITEM_HONEY_BOTTLE_DRINK, 1f, 1f);

            // Efecto visual al activarse
            player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 30, 0.3, 0.7, 0.3, 0.01);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getMetadataManager().markPlayerNoFallDamage(uuid, false);
                player.sendMessage(ChatColor.GRAY + "La protección contra caídas ha desaparecido.");
            }, 1200L); // 1 minuto = 1200 ticks
        }
    }
}

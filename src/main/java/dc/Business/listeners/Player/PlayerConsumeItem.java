package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.DeathScape;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerConsumeItem implements Listener {

    private final PlayerController playerController;

    public PlayerConsumeItem(PlayerController playerController) {
        this.playerController = playerController;
    }

    @EventHandler
    public void onPlayerConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        // Verifica si el ítem es el único especial
        if (consumedItem.hasItemMeta() && consumedItem.getItemMeta().hasDisplayName() &&
                consumedItem.getItemMeta().getDisplayName().equals(ChatColor.LIGHT_PURPLE + "Fruta Vital Mística")) {

            // Da 3 corazones extra temporales
            double currentHealth = player.getHealth();
            double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();

            // Añade corazones temporales (sin exceder el máximo del jugador)
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth + 6); // +3 corazones
            player.setHealth(Math.min(currentHealth + 6, maxHealth + 6)); // Ajusta salud actual

            player.sendMessage(ChatColor.GOLD + "¡Has ganado 3 corazones temporales durante 5 minutos!");

            /*
            // Programa la eliminación de los corazones temporales después de 5 minutos
            new BukkitRunnable() {
                @Override
                public void run() {
                    double newMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth - 6); // Quita los corazones extra

                    if (player.getHealth() > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());
                    }
                    player.sendMessage(ChatColor.RED + "Los corazones temporales han desaparecido.");
                }
            }.runTaskLater(plugin, 20L * 60 * 5); // 20 ticks = 1 segundo, 5 minutos = 20 * 60 * 5 ticks*/
        }
    }
}

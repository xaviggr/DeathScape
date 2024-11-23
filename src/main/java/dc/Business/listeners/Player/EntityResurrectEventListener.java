package dc.Business.listeners.Player;

import dc.Business.controllers.TotemController;
import dc.Business.controllers.PlayerController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Bukkit;

public class EntityResurrectEventListener implements Listener {

    private final TotemController totemController;

    public EntityResurrectEventListener(TotemController totemController, PlayerController playerController) {
        this.totemController = totemController;
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerInventory inventory = player.getInventory();

            // Check if the player has a Totem of Undying
            boolean hasTotem = (inventory.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING ||
                    inventory.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING);

            if (!hasTotem) {
                return; // Exit if no Totem of Undying
            }

            // Check if the Totem resurrection succeeds
            TotemController.Result result = totemController.willTotemSucceed();
            int successProbability = totemController.getConfig().getInt("config.totem_success_probability");

            if (!result.isSuccess()) {
                event.setCancelled(true); // Cancel resurrection event
                totemController.handleTotemFailure(player, result.getRandomValue());
            } else {
                // Send success messages to player and broadcast
                sendTotemSuccessMessages(player, result, successProbability);
            }
        }
    }

    private void sendTotemSuccessMessages(Player player, TotemController.Result result, int successProbability) {
        String successMessage = String.format("¡El tótem te salvó con una probabilidad de éxito de %d/%d!", result.getRandomValue(), successProbability);
        player.sendMessage(successMessage);

        // Broadcast to all players
        String broadcastMessage = String.format("¡%s sobrevivió gracias a su tótem con una probabilidad de éxito de %d/%d!", player.getName(), result.getRandomValue(), successProbability);
        Bukkit.broadcastMessage(broadcastMessage);

        // Play sound to all players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
    }
}

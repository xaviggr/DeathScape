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

/**
 * Listener for the `EntityResurrectEvent`. Handles the behavior of players resurrecting
 * using a Totem of Undying and checks if the resurrection succeeds based on probability.
 */
public class EntityResurrectEventListener implements Listener {

    private final TotemController totemController;

    /**
     * Constructs an `EntityResurrectEventListener` with the required controllers.
     *
     * @param totemController The controller managing Totem of Undying probabilities and behavior.
     * @param playerController The controller managing player-related actions (not directly used here).
     */
    public EntityResurrectEventListener(TotemController totemController, PlayerController playerController) {
        this.totemController = totemController;
    }

    /**
     * Handles the `EntityResurrectEvent` triggered when a player attempts to resurrect with a Totem of Undying.
     * Checks if the resurrection succeeds based on the configured probability and handles both success and failure.
     *
     * @param event The `EntityResurrectEvent` triggered when an entity attempts to resurrect.
     */
    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerInventory inventory = player.getInventory();

            // Check if the player has a Totem of Undying in hand
            boolean hasTotem = (inventory.getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING ||
                    inventory.getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING);

            if (!hasTotem) {
                return; // Exit if the player does not have a Totem of Undying
            }

            // Check if the Totem resurrection succeeds
            TotemController.Result result = totemController.willTotemSucceed();
            int successProbability = totemController.getConfig().getInt("config.totem_success_probability");

            if (!result.isSuccess()) {
                event.setCancelled(true); // Cancel the resurrection event
                totemController.handleTotemFailure(player, result.getRandomValue()); // Handle failure
            } else {
                // Handle successful resurrection
                sendTotemSuccessMessages(player, result, successProbability);
            }
        }
    }

    /**
     * Sends messages and feedback to the player and the server when the Totem of Undying successfully resurrects a player.
     *
     * @param player            The player who resurrected using the Totem of Undying.
     * @param result            The result of the resurrection probability check.
     * @param successProbability The probability of success configured in the plugin.
     */
    private void sendTotemSuccessMessages(Player player, TotemController.Result result, int successProbability) {
        // Success message for the player
        String successMessage = String.format("¡El tótem te salvó con una probabilidad de éxito de %d/%d!", result.getRandomValue(), successProbability);
        player.sendMessage(successMessage);

        // Broadcast message to all players
        String broadcastMessage = String.format("¡%s sobrevivió gracias a su tótem con una probabilidad de éxito de %d/%d!", player.getName(), result.getRandomValue(), successProbability);
        Bukkit.broadcastMessage(broadcastMessage);

        // Play sound for all players
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
        }
    }
}

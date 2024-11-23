package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerKillListener implements Listener {

    private final PlayerController playerController;

    public PlayerKillListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        Bukkit.getConsoleSender().sendMessage("Entity killed: " + entity.getType().name());

        // Check if the entity was killed by a player
        Player player = entity.getKiller();  // The player who killed the entity

        // If the entity was killed by a player
        if (player != null) {
            // Call another method to handle the monster type and reward points
            if (entity instanceof Monster) {
                handleMonsterKill(player, (Monster) entity);  // Pass the player and the specific monster
            }

            Message.enviarMensajeColorido(player, "You killed a " + entity.getType().name() + "!", ChatColor.GREEN);

            // If you want to do something with the items dropped by the entity
            /*
            for (ItemStack item : event.getDrops()) {

            }*/
        }
    }

    // Method to handle the monster kill and filter by type
    private void handleMonsterKill(Player player, Monster monster) {
        String monsterType = monster.getType().name(); // Get the type of the monster as a string
        int points = 0;  // Initialize points to 0

        // Handle different monster types and assign points accordingly
        switch (monsterType) {
            case "ZOMBIE":
                // Example: Add points for killing a zombie
                points = 10;
                player.sendMessage("You gained 10 points for killing a Zombie!");
                break;
            case "SKELETON":
                // Example: Add points for killing a skeleton
                points = 15;
                player.sendMessage("You gained 15 points for killing a Skeleton!");
                break;
            case "CREEPER":
                // Example: Add points for killing a creeper
                points = 20;
                player.sendMessage("You gained 20 points for killing a Creeper!");
                break;
            case "SPIDER":
                // Example: Add points for killing a spider
                points = 12;
                player.sendMessage("You gained 12 points for killing a Spider!");
                break;
            // Add more cases here for other monster types as needed
            default:
                // If no specific case matches, give default points for a monster
                points = 5;
                player.sendMessage("You gained 5 points for killing a " + monsterType + "!");
                break;
        }

        // Add points to the player using your method (you can customize this)
        playerController.addPointsToPlayer(player, points);
    }
}

package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Listener for handling player kills. Tracks when a player kills a monster
 * and awards points based on the monster type.
 */
public class PlayerKillListener implements Listener {

    private final PlayerController playerController;

    /**
     * Constructs a `PlayerKillListener` with the required `PlayerController`.
     *
     * @param playerController The controller managing player-related functionality.
     */
    public PlayerKillListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    /**
     * Handles the `EntityDeathEvent` triggered when an entity dies.
     * Checks if the entity was killed by a player and, if it's a monster, awards points.
     *
     * @param event The `EntityDeathEvent` triggered when an entity dies.
     */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if the entity was killed by a player
        Player player = entity.getKiller();

        if (player != null) {
            // Handle monster kills specifically
            if (entity instanceof Monster) {
                handleMonsterKill(player, (Monster) entity);
            } else {
                player.sendMessage("You gained 5 deathpoints for killing a " + entity.getType().name() + "!");
                playerController.addPointsToPlayer(player, 5);
            }
        }
    }

    /**
     * Handles the logic for awarding points to a player based on the type of monster they killed.
     *
     * @param player  The player who killed the monster.
     * @param monster The monster that was killed.
     */
    private void handleMonsterKill(Player player, Monster monster) {
        String monsterType = monster.getType().name(); // Get the monster type as a string
        int points; // Points to award

        // Assign points based on the monster type
        switch (monsterType) {
            case "ZOMBIE":
                points = 10;
                player.sendMessage("You gained 10 deathpoints for killing a Zombie!");
                break;
            case "SKELETON":
                points = 15;
                player.sendMessage("You gained 15 deathpoints for killing a Skeleton!");
                break;
            case "CREEPER":
                points = 20;
                player.sendMessage("You gained 20 deathpoints for killing a Creeper!");
                break;
            case "SPIDER":
                points = 12;
                player.sendMessage("You gained 12 deathpoints for killing a Spider!");
                break;
            default:
                points = 5; // Default points for other monsters
                player.sendMessage("You gained 5 deathpoints for killing a " + monsterType + "!");
                break;
        }



        // Add points to the player using the PlayerController
        playerController.addPointsToPlayer(player, points);
    }
}
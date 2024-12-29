package dc.Business.player;

import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * Handles player deaths in the game. Includes features for broadcasting death messages,
 * banning players, and creating statues at death locations.
 */
public class PlayerDeath implements Listener {

    private final DeathScape plugin;

    /**
     * Constructor to initialize the `PlayerDeath` class.
     * Registers event listeners with the server.
     *
     * @param plugin The DeathScape plugin instance.
     */
    public PlayerDeath(DeathScape plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin); // Register events
    }

    /**
     * Handles player death events. Broadcasts death messages, bans players (if applicable),
     * and creates a statue at the player's death location.
     *
     * @param player The player who has died.
     */
    public void Dead(Player player) {
        String victim = player.getName();

        String deathTitle = MainConfigManager.getInstance().getDeathMessageTitle();
        String deathSubtitle = MainConfigManager.getInstance().getDeathMessageSubtitle();
        deathSubtitle.replace("%player%", victim);

        Message.sendTitleToAllPlayers(deathTitle, deathSubtitle, 10, 40, 10); // Send a title to all players
        Message.playSoundToAllPlayers(Sound.ENTITY_WITHER_DEATH); // Play a sound to all players

        // Create a statue at the player's death location
        createStatueOnDeath(player);

        // Update player status in the database
        PlayerEditDatabase.setPlayerAsDeath(player);
        PlayerEditDatabase.setPlayerBanDate(player);

        player.spigot().respawn(); // Respawn the player
    }

    /**
     * Creates a statue at the location where the player died.
     * The statue consists of a crying obsidian block, a chain, a player skull,
     * and plays a sound effect at the death location.
     *
     * @param player The player who has died.
     */
    private void createStatueOnDeath(Player player) {
        World world = player.getWorld();
        Block deathLocationBlock = player.getLocation().getBlock();

        // Place a crying obsidian block below the death location
        Block cryingObsidianBlock = deathLocationBlock.getRelative(0, -1, 0);
        cryingObsidianBlock.setType(Material.CRYING_OBSIDIAN);

        // Place a chain block at the death location
        deathLocationBlock.setType(Material.CHAIN);

        // Place a player head block above the chain
        Block skullBlock = deathLocationBlock.getRelative(0, 1, 0);
        skullBlock.setType(Material.PLAYER_HEAD);

        // Set the player head to represent the deceased player
        try {
            Skull skull = (Skull) skullBlock.getState();
            skull.setOwningPlayer(player);
            skull.update();
        } catch (ClassCastException e) {
            Bukkit.getLogger().severe("Error while creating the statue head for " + player.getName() + ": " + e.getMessage());
        }

        // Play a sound at the death location
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }
}

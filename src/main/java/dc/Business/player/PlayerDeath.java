package dc.Business.player;

import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerEditDatabase;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import static org.bukkit.Bukkit.getServer;

public class PlayerDeath implements Listener {

    private final DeathScape plugin;
    private final PlayerBan playerBan;

    // Constructor to initialize the plugin and player ban handler
    public PlayerDeath(DeathScape plugin, PlayerBan playerBan) {
        this.plugin = plugin;
        this.playerBan = playerBan;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);  // Register events for this listener
    }

    /**
     * Called when a player dies. This handles death messages, bans (if applicable), and creates a death statue.
     * @param player The player who died.
     */
    public void Dead(Player player) {
        String victim = player.getName();

        // Notify all players about the death
        for (Player checkingPlayer : Bukkit.getOnlinePlayers()) {
            String deathTitle = MainConfigManager.getInstance().getDeathMessageTitle();
            String deathSubtitle = MainConfigManager.getInstance().getDeathMessageSubtitle();

            // Send title and subtitle to all players
            checkingPlayer.sendTitle(deathTitle, deathSubtitle.replace("%player%", victim), 20, 100, 20);
            checkingPlayer.playSound(checkingPlayer.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f);
        }

        // Handle admin case (do not ban, just spectate)
        if (player.isOp()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            // If not an admin, ban the player
            handlePlayerBan(player);
        }

        // Create a statue at the player's death location
        createStatueOnDeath(player);

        // Update player status in database
        PlayerEditDatabase.setPlayerAsDeath(player);
    }

    /**
     * Handles banning the player after they die.
     * @param player The player to be banned.
     */
    private void handlePlayerBan(Player player) {
        if (player.isOnline()) {
            playerBan.Ban(player, ChatColor.RED + MainConfigManager.getInstance().getBanMessage(), null);
            player.kickPlayer(ChatColor.RED + MainConfigManager.getInstance().getBanMessage());
        } else {
            // If player is offline, immediately ban them
            playerBan.Ban(player, ChatColor.RED + MainConfigManager.getInstance().getBanMessage(), null);
        }
    }

    /**
     * Creates a statue at the location where the player died.
     * This includes a bedrock block, chain, player skull, and an armor stand with the player's name.
     * @param player The player who died.
     */
    private void createStatueOnDeath(Player player) {
        World world = player.getWorld();
        Block deathLocationBlock = player.getLocation().getBlock();

        // Colocar un bloque de obsidiana llorosa debajo de la ubicación de muerte
        Block cryingObsidianBlock = deathLocationBlock.getRelative(0, -1, 0);
        cryingObsidianBlock.setType(Material.CRYING_OBSIDIAN);

        // Colocar un bloque de cadena en la ubicación de muerte
        deathLocationBlock.setType(Material.CHAIN);

        // Colocar una cabeza de jugador encima del bloque de cadena
        Block skullBlock = deathLocationBlock.getRelative(0, 1, 0);
        skullBlock.setType(Material.PLAYER_HEAD);

        // Configurar la cabeza del jugador en el bloque
        try {
            Skull skull = (Skull) skullBlock.getState();
            skull.setOwningPlayer(player);
            skull.update();
        } catch (ClassCastException e) {
            Bukkit.getLogger().severe("Error al crear la cabeza de la estatua para " + player.getName() + ": " + e.getMessage());
        }

        // Reproducir un sonido en la ubicación de muerte
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }
}

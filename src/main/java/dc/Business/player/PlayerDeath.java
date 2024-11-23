package dc.Business.player;

import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

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
            Message.enviarMensajeColorido(player, "El jugador " + victim + " ha muerto!", ChatColor.RED);

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
        PlayerEditDatabase.setPlayerCoords(player);

        // Trigger server-specific checks
        plugin.getServerData().checkDay();
    }

    /**
     * Handles banning the player after they die.
     * @param player The player to be banned.
     */
    private void handlePlayerBan(Player player) {
        if (player.isOnline()) {
            // Schedule ban and kick the player after a short delay
            getServer().getScheduler().runTaskLater(plugin, () -> {
                playerBan.Ban(player, ChatColor.RED + MainConfigManager.getInstance().getBanMessage(), null);
                player.kickPlayer(ChatColor.RED + MainConfigManager.getInstance().getBanMessage());
            }, 2 * 20L);  // Delay of 2 seconds
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

        // Place a bedrock block below the death location
        Block bedrockBlock = deathLocationBlock.getRelative(0, -1, 0);
        bedrockBlock.setType(Material.BEDROCK);

        // Place a chain block at the death location
        deathLocationBlock.setType(Material.CHAIN);

        // Place a player skull above the chain block
        Block skullBlock = deathLocationBlock.getRelative(0, 1, 0);
        skullBlock.setType(Material.PLAYER_HEAD);

        // Set the player's head on the skull block
        try {
            Skull skull = (Skull) skullBlock.getState();
            skull.setOwningPlayer(player);
            skull.update();
        } catch (ClassCastException e) {
            Bukkit.getLogger().severe("Error al crear la cabeza de la estatua para " + player.getName() + ": " + e.getMessage());
        }

        // Create an armor stand with the player's name above the skull
        ArmorStand nameStand = (ArmorStand) world.spawnEntity(bedrockBlock.getLocation().add(0.5, 0.5, 0.5), EntityType.ARMOR_STAND);
        nameStand.setCustomName(player.getName());
        nameStand.setCustomNameVisible(true);
        nameStand.setGravity(false);
        nameStand.setVisible(false);

        // Generate a fire aura around the death location
        generateFireAura(player, deathLocationBlock);

        // Play a sound at the death location
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }

    /**
     * Generates a fire aura around the player's death location using particles.
     * @param player The player who died.
     * @param baseBlock The block where the aura should be generated.
     */
    private void generateFireAura(Player player, Block baseBlock) {
        World world = baseBlock.getWorld();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 200) {  // Stop after 200 ticks (about 10 seconds)
                    cancel();
                    return;
                }

                // Generate flame particles around the death location
                world.spawnParticle(Particle.FLAME, baseBlock.getLocation().add(0.5, 1.5, 0.5), 20, 0.5, 0.5, 0.5, 0);

                count++;
            }
        }.runTaskTimer(plugin, 0, 10);  // Run every 10 ticks
    }
}

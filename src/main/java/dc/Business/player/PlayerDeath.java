package dc.Business.player;

import dc.DeathScape;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Bukkit.getServer;

public class PlayerDeath {

    private final DeathScape plugin;
    private final PlayerBan playerBan;
    //Constructor
    public PlayerDeath(DeathScape plugin, PlayerBan playerBan) {
        this.plugin = plugin;
        this.playerBan = playerBan;
    }

    public void Dead(Player player) {
        String victim = player.getName ();

        for (Player checking_player: Bukkit.getOnlinePlayers ()) {
            Message.enviarMensajeColorido (player, "El jugador " + victim + " ha muerto!", "rojo");

            String ServerMessageTitle = plugin.getMainConfigManager().getDeathMessageTitle();
            String ServerMessageSubtitle = plugin.getMainConfigManager().getDeathMessageSubtitle();

            checking_player.sendTitle (ServerMessageTitle, ServerMessageSubtitle.replace ("%player%", victim), 20, 20 * 5, 20);
            checking_player.playSound (checking_player.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f);
        }

        //Si es admin no lo banea
        if (player.isOp ()) {
            player.setGameMode (GameMode.SPECTATOR);
        } else {
            //Si esta conectado se le expulsa.
            if (player.isOnline ()) {
                getServer().getScheduler().runTaskLater(plugin, () -> {
                    playerBan.Ban (player, ChatColor.RED + plugin.getMainConfigManager ().getBanMessage (), null);
                    player.kickPlayer (ChatColor.RED + plugin.getMainConfigManager ().getBanMessage ());
                }, 2 * 20L);

            } else {
                playerBan.Ban (player, ChatColor.RED + plugin.getMainConfigManager ().getBanMessage (), null);
            }
        }

        createStatueOnDeath(player);
        PlayerEditDatabase.setPlayerAsDeath (player);
        PlayerEditDatabase.setPlayerCoords(player);
        plugin.getServerData().checkDay();
    }

    private void createStatueOnDeath(Player player) {
        /*
        Location l = player.getEyeLocation().clone();
        if (l.getY() < 3) {
            l.setY(3);
        }
        Block skullBlock = l.getBlock();
        skullBlock.setType(Material.PLAYER_HEAD);

        Skull skullState = (Skull) skullBlock.getState();
        skullState.setOwningPlayer(player);
        skullState.update();

        Rotatable rotatable = (Rotatable) skullBlock.getBlockData();
        rotatable.setRotation(player.getFacing());
        skullBlock.setBlockData(rotatable);

        skullBlock.getRelative(BlockFace.DOWN).setType(Material.NETHER_BRICK_FENCE);
        skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);*/

        World world = player.getWorld();
        Block deathLocationBlock = player.getLocation().getBlock();

        // Colocar bedrock en el lugar de muerte
        deathLocationBlock.setType(Material.BEDROCK);

        // Colocar la cabeza del jugador en el bedrock
        Block skullBlock = deathLocationBlock.getRelative(0, 1, 0);
        skullBlock.setType(Material.PLAYER_HEAD);
        Skull skull = (Skull) skullBlock.getState();
        skull.setOwningPlayer(player);
        skull.update();

        // Generar el aura oscura
        generateDarkAura(player, deathLocationBlock);

        // Mensaje o sonido al morir
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }

    private void generateDarkAura(Player player, Block baseBlock) {
        World world = baseBlock.getWorld();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 200) {  // Duración del aura (200 ticks)
                    cancel();
                    return;
                }

                // Generar partículas de oscuridad alrededor
                world.spawnParticle(Particle.SMOKE_LARGE, baseBlock.getLocation().add(0.5, 1.5, 0.5), 20, 0.5, 0.5, 0.5, 0);
                world.spawnParticle(Particle.FLAME, baseBlock.getLocation().add(0.5, 1.5, 0.5), 15, 0.3, 0.3, 0.3, 0.01);

                count++;
            }
        }.runTaskTimer(plugin, 0, 10); // Cada 10 ticks se ejecuta
    }
}

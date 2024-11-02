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
        /*
        for (Player checking_player: Bukkit.getOnlinePlayers ()) {
            Message.enviarMensajeColorido (player, "El jugador " + victim + " ha muerto!", "rojo");

            String ServerMessageTitle = plugin.getMainConfigManager().getDeathMessageTitle();
            String ServerMessageSubtitle = plugin.getMainConfigManager().getDeathMessageSubtitle();

            checking_player.sendTitle (ServerMessageTitle, ServerMessageSubtitle.replace ("%player%", victim), 20, 20 * 5, 20);
            checking_player.playSound (checking_player.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f);
        }*/

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
        skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
    }
}

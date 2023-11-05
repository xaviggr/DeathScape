package dc.player;

import dc.DeathScape;
import dc.config.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;

import static org.bukkit.Bukkit.getServer;

public class PlayerDeath {

    public static void Dead(Player player, DeathScape plugin) {
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
                    PlayerBan.Ban (player, ChatColor.RED + plugin.getMainConfigManager ().getBanMessage (), null);
                    player.kickPlayer (ChatColor.RED + plugin.getMainConfigManager ().getBanMessage ());
                }, 5 * 20L);

            } else {
                PlayerBan.Ban (player, ChatColor.RED + plugin.getMainConfigManager ().getBanMessage (), null);
            }
        }
        PlayerCreateStatueOnDeath.Create(player);
        PlayerEditDatabase.setPlayerAsDeath (player);
    }
}

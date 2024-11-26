package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.groups.GroupData;
import dc.Business.groups.Permission;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.Business.player.PlayerData;
import dc.Persistence.config.MainConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.Objects;

public class PlayerJoinEventListener implements Listener {

    private final PlayerController playerController;

    public PlayerJoinEventListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostAddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();
        GroupData groupData = null;

        // Load or create player data
        PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        if (playerData == null) {
            playerData = new PlayerData(player.getName(), false, 0, hostAddress, "0", player.getUniqueId(), "0", "0", "0", 0, "default");
            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error loading your data, please contact an administrator.");
            }
        } else {
            groupData = GroupDatabase.getGroupData((playerData.getGroup()));
            if (playerData.isDead() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "You are dead, please contact an administrator.");
                return;
            }
            if (!Objects.equals(playerData.getHostAddress(), hostAddress) && MainConfigManager.getInstance().isKickIfIpChanged() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Your IP has changed, please contact an administrator.");
                return;
            }
            if (groupData == null) {
                player.kickPlayer(ChatColor.RED + "You don't have a group assigned, please contact an administrator.");
                return;
            }
        }

        String prefix = Objects.requireNonNull(groupData).getPrefix();
        prefix = ChatColor.translateAlternateColorCodes('&', prefix);
        event.setJoinMessage(prefix + " " + ChatColor.YELLOW + player.getName() + " ha entrado al mundo DeathScape");
        playerController.setPlayerRank(player, prefix);
        // Set up TabList and track connection time
        playerController.setUpTabList(player);
    }
}

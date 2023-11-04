package dc.listeners;

import dc.utils.Search;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (Search.buscarPalabraProhibida(message)) {
            event.setCancelled(true);
            player.sendMessage("No puedes decir eso!");
        }
    }
}

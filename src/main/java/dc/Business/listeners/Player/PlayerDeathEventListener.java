package dc.Business.listeners.Player;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.WeatherController;
import dc.Business.controllers.AnimationController;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import java.util.Objects;

public class PlayerDeathEventListener implements Listener {

    private final PlayerController playerController;
    private final WeatherController weatherController;
    private final AnimationController animationController;

    public PlayerDeathEventListener(PlayerController playerController, WeatherController weatherController, AnimationController animationController) {
        this.playerController = playerController;
        this.weatherController = weatherController;
        this.animationController = animationController;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());
        playerController.setPlayerAsDead(player);
        weatherController.updateStormOnPlayerDeath();
        animationController.startDeathAnimation(player);
    }
}

package dc.Business.listeners;

import dc.Business.controllers.PlayerController;
import dc.Business.controllers.ServerController;
import dc.Business.controllers.StormController;
import dc.Business.controllers.TotemController;
import dc.DeathScape;
import dc.Persistence.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


public class PlayerListener implements Listener {

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final StormController stormController;
    private final TotemController totemController;
    private final Set<Player> sleepingPlayers = new HashSet<>(); // Conjunto para jugadores durmiendo

    public PlayerListener(DeathScape plugin, ServerController serverController, PlayerController playerController, StormController stormController, TotemController totemController) {
        this.plugin = plugin;
        this.playerController = playerController;
        this.stormController = stormController;
        this.totemController = totemController;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = Objects.requireNonNull(event.getEntity().getPlayer());
        playerController.setPlayerAsDead(player);
        stormController.updateStormOnPlayerDeath();
    }

    @EventHandler
    public void onPlayerJoinServer(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String hostaddress = Objects.requireNonNull(player.getAddress()).getAddress().getHostAddress();

        if (PlayerDatabase.getPlayerDataFromDatabase(player.getName()) == null) {
            PlayerData playerData = new PlayerData(player.getName(), false, 0, hostaddress, "0", player.getUniqueId(), "0", "0", "0", 0);
            if (!PlayerDatabase.addPlayerDataToDatabase(playerData)) {
                player.kickPlayer(ChatColor.RED + "Error al cargar tus datos, contacta con un administrador.");
            }
        } else {
            PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
            assert playerData != null;
            if (playerData.isDead() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Estás muerto, contacta con un administrador.");
            }
            if (!Objects.equals(playerData.getHostAddress(), hostaddress) && plugin.getMainConfigManager().isKick_if_ip_changed() && !player.isOp()) {
                player.kickPlayer(ChatColor.RED + "Tu IP ha cambiado, contacta con un administrador.");
            }
        }

        if (player.isOnline()) {
            plugin.time_of_connection.put(player.getName(), System.currentTimeMillis());
        }

        playerController.setUpTabList(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();

        if (plugin.time_of_connection.containsKey(playerName)) {
            long initTime = plugin.time_of_connection.get(playerName);
            long actualTime = System.currentTimeMillis();
            long onlineTime = actualTime - initTime;

            int segundos = Integer.parseInt(String.valueOf((int) (onlineTime / 1000) % 60));
            int minutos = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60)) % 60)));
            int horas = Integer.parseInt(String.valueOf((int) ((onlineTime / (1000 * 60 * 60)) % 24)));
            PlayerEditDatabase.setPlayerTimePlayed(player, segundos, minutos, horas);
            plugin.time_of_connection.remove(playerName);
        }

        PlayerEditDatabase.setPlayerCoords(player);
    }

    // Añadido: evento para manejar el sueño
    @EventHandler
    public void onPlayerSleep(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        sleepingPlayers.add(player); // Añadir al jugador a la lista de durmiendo

        // Obtener el mundo donde está el jugador
        org.bukkit.World world = player.getWorld();

        // Cambiar el tiempo a día si hay al menos un jugador durmiendo
        if (sleepingPlayers.size() == 1 && (world.getTime() >= 13000 && world.getTime() <= 23000)) { // Solo cambiar a día si es el primero en dormir
            Bukkit.getWorlds().forEach(w -> {
                w.setTime(0); // Establece la hora a 0 (día)
            });
            Message.enviarMensajeColorido(player, "Has hecho que el día llegue!", "dorado");
        }
    }

    // Añadido: evento para manejar el despertar
    @EventHandler
    public void onPlayerWake(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        sleepingPlayers.remove(player); // Eliminar al jugador que se despertó
    }

    @EventHandler
    public void onEntityResurrect(EntityResurrectEvent event) {
        Entity entity = event.getEntity();

        // Verifica si la entidad es un jugador
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // Llama al método willTotemSucceed del TotemController
            TotemController.Result result = totemController.willTotemSucceed();

            // Si el tótem no tiene éxito, cancela la resurrección
            if (!result.isSuccess()) {
                event.setCancelled(true); // Cancela el evento de resurrección
                totemController.handleTotemFailure(player, result.getRandomValue()); // Maneja la falla del tótem
            } else {
                // El tótem ha funcionado, se puede añadir lógica adicional si es necesario
                player.sendMessage("¡El tótem te ha salvado con una probabilidad de " + result.getRandomValue() + "/" +
                        totemController.getConfig().getInt("config.totem_success_probability") + "!");
                // Mensaje a todos los jugadores
                String broadcastMessage = ChatColor.GREEN + "¡El jugador " + ChatColor.BLUE + player.getName() + ChatColor.GREEN + " ha sobrevivido gracias a su tótem con una probabilidad de " +
                        ChatColor.BLUE + result.getRandomValue() + ChatColor.WHITE + "/" + ChatColor.RED + plugin.getConfig().getInt("config.totem_success_probability") + ChatColor.GREEN + "!";
                Bukkit.broadcastMessage(broadcastMessage);

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
                }
            }
        }
    }
}

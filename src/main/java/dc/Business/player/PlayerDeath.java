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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

import static org.bukkit.Bukkit.getServer;

public class PlayerDeath implements Listener {

    private final DeathScape plugin;
    private final PlayerBan playerBan;

    // Constructor
    public PlayerDeath(DeathScape plugin, PlayerBan playerBan) {
        this.plugin = plugin;
        this.playerBan = playerBan;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void Dead(Player player) {
        String victim = player.getName();

        for (Player checking_player : Bukkit.getOnlinePlayers()) {
            Message.enviarMensajeColorido(player, "El jugador " + victim + " ha muerto!", ChatColor.RED);

            String ServerMessageTitle = MainConfigManager.getInstance().getDeathMessageTitle();
            String ServerMessageSubtitle = MainConfigManager.getInstance().getDeathMessageSubtitle();

            checking_player.sendTitle(ServerMessageTitle, ServerMessageSubtitle.replace("%player%", victim), 20, 20 * 5, 20);
            checking_player.playSound(checking_player.getLocation(), Sound.ENTITY_BLAZE_DEATH, Float.MAX_VALUE, -0.1f);
        }

        // Si es admin no lo banea
        if (player.isOp()) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            // Si esta conectado se le expulsa.
            if (player.isOnline()) {
                getServer().getScheduler().runTaskLater(plugin, () -> {
                    playerBan.Ban(player, ChatColor.RED + MainConfigManager.getInstance().getBanMessage(), null);
                    player.kickPlayer(ChatColor.RED + MainConfigManager.getInstance().getBanMessage());
                }, 2 * 20L);
            } else {
                playerBan.Ban(player, ChatColor.RED + MainConfigManager.getInstance().getBanMessage(), null);
            }
        }

        createStatueOnDeath(player);
        PlayerEditDatabase.setPlayerAsDeath(player);
        PlayerEditDatabase.setPlayerCoords(player);
        plugin.getServerData().checkDay();
    }

    private void createStatueOnDeath(Player player) {
        World world = player.getWorld();
        Block deathLocationBlock = player.getLocation().getBlock();

        // Colocar bedrock un bloque por debajo del lugar de muerte
        Block bedrockBlock = deathLocationBlock.getRelative(0, -1, 0);
        bedrockBlock.setType(Material.BEDROCK);

        // Colocar la cadena en la posición del jugador
        deathLocationBlock.setType(Material.CHAIN);

        // Colocar la cabeza del jugador encima de la cadena
        Block skullBlock = deathLocationBlock.getRelative(0, 1, 0);
        skullBlock.setType(Material.PLAYER_HEAD);

        // Manejo de la cabeza del jugador
        try {
            Skull skull = (Skull) skullBlock.getState();
            skull.setOwningPlayer(player);
            skull.update();
        } catch (ClassCastException e) {
            Bukkit.getLogger().severe("Error al crear la cabeza de la estatua para " + player.getName() + ": " + e.getMessage());
            // Opcionalmente, puedes realizar una acción alternativa aquí.
        }

        // Crear un ArmorStand con el nombre del jugador encima de la cabeza
        ArmorStand nameStand = (ArmorStand) world.spawnEntity(bedrockBlock.getLocation().add(0.5, 0.5, 0.5), EntityType.ARMOR_STAND);
        nameStand.setCustomName(player.getName());
        nameStand.setCustomNameVisible(true);
        nameStand.setGravity(false);
        nameStand.setVisible(false);

        // Generar el aura de fuego
        generateFireAura(player, deathLocationBlock);

        // Mensaje o sonido al morir
        world.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
    }


    private void generateFireAura(Player player, Block baseBlock) {
        World world = baseBlock.getWorld();

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= 200) {  // Duración del aura (200 ticks)
                    cancel();
                    return;
                }

                // Generar partículas de fuego alrededor
                world.spawnParticle(Particle.FLAME, baseBlock.getLocation().add(0.5, 1.5, 0.5), 20, 0.5, 0.5, 0.5, 0);

                count++;
            }
        }.runTaskTimer(plugin, 0, 10); // Cada 10 ticks se ejecuta
    }
}
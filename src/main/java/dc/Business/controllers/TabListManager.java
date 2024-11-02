package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TabListManager implements Listener {
    private final DeathScape plugin;
    private int animationIndex = 0; // Índice para controlar la animación

    public TabListManager(DeathScape plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin); // Asegúrate de registrar los eventos aquí
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateTabList(player);

        // Iniciar la animación al unirse un jugador
        startAnimation(player);
    }

    private void startAnimation(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                }
                updateTabList(player);
            }
        }.runTaskTimer(plugin, 0, 10); // Actualiza cada 10 ticks (0.5 segundos)
    }

    private void updateTabList(Player player) {
        String playerName = player.getName();
        String rank = getRank(player); // Obtiene el rango del jugador
        String welcomeMessage = getAnimatedWelcomeMessage(playerName); // Obtén el mensaje animado con el nombre del jugador

        String header = ChatColor.DARK_AQUA + "=== DEATHSCAPE 4: LOST CHAPTER ===\n" +
                ChatColor.BOLD + ChatColor.GRAY + ">> " + welcomeMessage + " <<\n" +  // Aquí añadimos el color azul al nombre
                ChatColor.GRAY + "Conectados: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "\n" +
                ChatColor.GOLD + "Admins: " + ChatColor.YELLOW + getAdminCount() + "\n";

        String footer = ChatColor.DARK_GRAY + "Fecha: " + ChatColor.BLUE + getCurrentDate() + "\n" +
                ChatColor.GREEN + "Tu Ping: " + ChatColor.LIGHT_PURPLE + player.getPing() + " ms\n" +
                ChatColor.GRAY + "Tienda: " + ChatColor.AQUA + "https://deathscapemc.tebex.io";

        // Añadir rango y corazones a la lista de jugadores
        String playersList = getPlayersList();

        // Actualiza el tabulador con el encabezado y pie de página
        player.setPlayerListHeaderFooter(header, footer);
    }

    private String getPlayersList() {
        StringBuilder playerList = new StringBuilder();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String rank = getRank(onlinePlayer);
            String health = String.valueOf((int) onlinePlayer.getHealth()); // Obtener la salud actual del jugador como número
            playerList.append(ChatColor.GRAY).append("[").append(rank).append("] ")
                    .append(onlinePlayer.getName()).append(" ")
                    .append(health).append(" HP ") // Aquí incluimos la salud del jugador
                    .append(onlinePlayer.getPing()).append(" ms\n");
        }
        return playerList.toString();
    }

    private String getRank(Player player) {
        // Aquí podrías implementar tu lógica para obtener el rango del jugador
        if (player.hasPermission("admin")) {
            return ChatColor.RED + "ADMIN";
        } else if (player.hasPermission("mod")) {
            return ChatColor.BLUE + "MOD";
        } else {
            return ChatColor.GREEN + "JUGADOR";
        }
    }

    private String getAnimatedWelcomeMessage(String playerName) {
        String baseMessage = "Bienvenido " + playerName + "!";
        StringBuilder animatedMessage = new StringBuilder();

        // Crea el mensaje animado
        for (int i = 0; i < baseMessage.length(); i++) {
            if (i == animationIndex) {
                animatedMessage.append('_'); // Reemplaza la letra actual por un guion bajo
            } else {
                animatedMessage.append(baseMessage.charAt(i)); // Mantiene la letra original
            }
        }

        // Mueve el índice de animación
        animationIndex++;
        if (animationIndex >= baseMessage.length()) {
            animationIndex = 0; // Reinicia el índice
        }

        return animatedMessage.toString();
    }

    private int getAdminCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("admin")) // Cambia "admin" al permiso real que usas para los administradores
                .count();
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new Date());
    }
}

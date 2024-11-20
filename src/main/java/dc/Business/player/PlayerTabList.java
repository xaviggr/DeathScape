package dc.Business.player;

import dc.Business.controllers.PlayerController;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.groups.GroupDatabase;
import dc.Persistence.player.PlayerDatabase;
import dc.utils.Info;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class PlayerTabList {

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final Map<Player, Integer> playerIndices = new HashMap<>();
    private int colorIndex = 0;

    public PlayerTabList(DeathScape plugin, PlayerController playerController) {
        this.plugin = plugin;
        this.playerController = playerController;
    }

    private final ChatColor[] colors = {
            ChatColor.YELLOW, ChatColor.GOLD
    };

    public void startAnimation(Player player) {
        playerIndices.put(player, 1); // Inicializa el índice para el jugador

        // Eliminar el jugador si no está en línea
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    playerIndices.remove(player); // Eliminar el jugador si no está en línea
                    cancel();
                }
                updateTabList(player, Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())));
            }
        };

        runnable.runTaskTimer(plugin, 0, 10); // Actualiza cada 10 ticks (0.5 segundos)
    }

    public void updateTabList(Player player, PlayerData playerData) {
        String modifiedHeader = createAnimatedHeader(player); // Crear el encabezado animado

        String footer = ChatColor.DARK_GRAY + "Fecha: " + ChatColor.BLUE + Info.getCurrentDate() + "\n" +
                ChatColor.GREEN + "Tu Ping: " + ChatColor.LIGHT_PURPLE + player.getPing() + " ms\n" +
                getColorfulShopText();

        int health = (int) player.getHealth();
        player.setPlayerListName(
                        ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(playerController.getGroupFromPlayer(player)))) +
                        ChatColor.WHITE + player.getName() +
                        ChatColor.RED + " " + health + "❤" +
                        ChatColor.YELLOW + " " + playerData.getPoints() + "⦿");
        player.setPlayerListHeaderFooter(modifiedHeader, footer);
    }

    private String createAnimatedHeader(Player player) {
        String welcomeMessage = MainConfigManager.getInstance().getWelcomeMessage();

        // Construir el título y las partes sin animación
        String staticHeader = ChatColor.DARK_AQUA + "=== DEATHSCAPE 4: LOST CHAPTER ===\n" +
                ChatColor.GRAY + "Conectados: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "\n" +
                ChatColor.GOLD + "Admins: " + ChatColor.YELLOW + Info.getAdminCount() + "\n" + ChatColor.GRAY + ChatColor.BOLD;

        // Parte animada para el mensaje de bienvenida
        String animatedMessage = ">> " + welcomeMessage.replace("%player%", player.getName()) + " <<";
        StringBuilder animatedHeader = new StringBuilder(animatedMessage);

        // Obtener el índice actual para el jugador
        int characterIndex = playerIndices.getOrDefault(player, 0);

        // Asegurarse de que el índice esté dentro de los límites de 'animatedMessage'
        if (characterIndex < animatedMessage.length()) {
            animatedHeader.setCharAt(characterIndex, '_');
            playerIndices.put(player, characterIndex + 1); // Actualizar el índice para el jugador
        } else {
            // Reiniciar el índice si excede la longitud del mensaje
            playerIndices.put(player, 1);
        }

        // Combinar el título, mensaje animado y la información adicional
        return staticHeader + animatedHeader.toString() + "\n";
    }

    private String getColorfulShopText() {
        String shopText = "https://deathscapemc.tebex.io";
        StringBuilder colorfulText = new StringBuilder();

        for (int i = 0; i < shopText.length(); i++) {
            ChatColor color = colors[(colorIndex + i) % colors.length]; // Aplicar color cíclico a cada letra
            colorfulText.append(color).append(shopText.charAt(i));
        }

        colorIndex = (colorIndex + 1) % colors.length; // Avanza el índice para el próximo ciclo
        return ChatColor.GRAY + "Tienda: " + colorfulText;
    }
}

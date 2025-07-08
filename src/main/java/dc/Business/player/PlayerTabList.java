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

/**
 * Handles the animated and dynamic tab list for players in the DeathScape server.
 * Includes real-time updates for player stats, welcome messages, and shop links.
 */
public class PlayerTabList {

    private final DeathScape plugin;
    private final PlayerController playerController;
    private final Map<Player, Integer> playerIndices = new HashMap<>(); // Tracks the animation index for each player
    private int colorIndex = 0; // Index for cycling colors in the shop link animation

    // Colors for animating the shop link text
    private final ChatColor[] colors = {
            ChatColor.YELLOW, ChatColor.GOLD
    };

    /**
     * Constructor for the PlayerTabList.
     *
     * @param plugin            The instance of the DeathScape plugin.
     * @param playerController  The controller for managing player-related features.
     */
    public PlayerTabList(DeathScape plugin, PlayerController playerController) {
        this.plugin = plugin;
        this.playerController = playerController;
    }

    /**
     * Starts the tab list animation for a player. Periodically updates the player's tab list
     * with animated headers and real-time stats.
     *
     * @param player The player for whom the tab list animation is started.
     */
    public void startAnimation(Player player) {
        playerIndices.put(player, 1); // Initialize the animation index for the player

        // Periodically update the tab list
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    playerIndices.remove(player); // Remove the player if they are offline
                    cancel(); // Stop the task
                }
                updateTabList(player, Objects.requireNonNull(PlayerDatabase.getPlayerDataFromDatabase(player.getName())));
            }
        };

        runnable.runTaskTimer(plugin, 0, 10); // Update every 10 ticks (0.5 seconds)
    }

    /**
     * Updates the player's tab list with dynamic headers, footers, and stats.
     *
     * @param player     The player whose tab list is updated.
     * @param playerData The player's data, including points and group.
     */
    public void updateTabList(Player player, PlayerData playerData) {
        String modifiedHeader = createAnimatedHeader(player); // Generate the animated header

        String footer = ChatColor.DARK_GRAY + "Fecha: " + ChatColor.BLUE + Info.getCurrentDate() + "\n" +
                ChatColor.GREEN + "Tu Ping: " + ChatColor.LIGHT_PURPLE + player.getPing() + " ms\n" +
                getColorfulShopText();

        int health = (int) player.getHealth();
        player.setPlayerListName(
                ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(GroupDatabase.getPrefixFromGroup(playerController.getGroupFromPlayer(player)))) +
                        " " + ChatColor.WHITE + player.getName() +
                        ChatColor.RED + " " + health + "❤" +
                        ChatColor.YELLOW + " " + playerData.getPoints() + "⦿");
        player.setPlayerListHeaderFooter(modifiedHeader, footer);
    }

    /**
     * Creates an animated header for the tab list. Includes a static part and
     * an animated part (welcome message with moving characters).
     *
     * @param player The player for whom the header is created.
     * @return The animated header as a string.
     */
    private String createAnimatedHeader(Player player) {
        String welcomeMessage = MainConfigManager.getInstance().getWelcomeMessage();

        // Static parts of the header
        String staticHeader = ChatColor.DARK_AQUA + "=== DEATHSCAPE 4: LOST CHAPTER ===\n" +
                ChatColor.GRAY + "Conectados: " + ChatColor.WHITE + Bukkit.getOnlinePlayers().size() + "\n" +
                ChatColor.GOLD + "Admins: " + ChatColor.YELLOW + Info.getAdminCount() + "\n" + ChatColor.GRAY + ChatColor.BOLD;

        // Animated welcome message
        String animatedMessage = ">> " + welcomeMessage.replace("%player%", player.getName()) + " <<";
        StringBuilder animatedHeader = new StringBuilder(animatedMessage);

        // Retrieve the current animation index for the player
        int characterIndex = playerIndices.getOrDefault(player, 0);

        // Replace a character with an underscore to create animation
        if (characterIndex < animatedMessage.length()) {
            animatedHeader.setCharAt(characterIndex, '_');
            playerIndices.put(player, characterIndex + 1); // Update the index for the player
        } else {
            // Reset the index if it exceeds the message length
            playerIndices.put(player, 1);
        }

        // Combine static and animated parts
        return staticHeader + animatedHeader + "\n";
    }

    /**
     * Generates a colorful, animated shop link for the footer.
     *
     * @return The colorful shop link as a string.
     */
    private String getColorfulShopText() {
        String shopText = "https://deathscapemc.tebex.io";
        StringBuilder colorfulText = new StringBuilder();

        // Apply a cyclic color pattern to each character in the shop link
        for (int i = 0; i < shopText.length(); i++) {
            ChatColor color = colors[(colorIndex + i) % colors.length];
            colorfulText.append(color).append(shopText.charAt(i));
        }

        // Update the color index for the next animation frame
        colorIndex = (colorIndex + 1) % colors.length;
        return ChatColor.GRAY + "Tienda: " + colorfulText;
    }
}

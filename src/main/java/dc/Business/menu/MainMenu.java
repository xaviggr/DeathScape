package dc.Business.menu;

import dc.DeathScape;
import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class MainMenu implements Listener {

    private final DeathScape plugin;

    public MainMenu(DeathScape plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Menú Principal");

        // Obtener datos del jugador
        PlayerData data = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
        String group = (data != null) ? data.getGroup() : "Desconocido";
        assert data != null;
        int daysPlayed = data.calculateDaysPlayed();

        // Calcular tiempo conectado
        long sessionTime = System.currentTimeMillis() - plugin.time_of_connection.getOrDefault(player.getName(), System.currentTimeMillis());
        int minutes = (int) (sessionTime / 1000 / 60);

        // Calcular posición en el ranking
        List<PlayerData> leaderboard = PlayerDatabase.getLeaderboard();
        int rank = 1;
        for (PlayerData pd : leaderboard) {
            if (pd.getName().equalsIgnoreCase(player.getName())) break;
            rank++;
        }

        // Información del jugador
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName(ChatColor.AQUA + "Información del jugador");
        infoMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Vida: " + (int) player.getHealth() + " ❤",
                ChatColor.GRAY + "Grupo: " + group,
                ChatColor.GRAY + "Tiempo conectado: " + minutes + " min",
                ChatColor.GRAY + "Ranking: " + rank + "º",
                ChatColor.GRAY + "Usa /deathscape help para más info"
        ));
        info.setItemMeta(infoMeta);
        menu.setItem(10, info);

        // Interacción con el juego (placeholder)
        ItemStack game = new ItemStack(Material.LEGACY_ENDER_PORTAL_FRAME);
        ItemMeta gameMeta = game.getItemMeta();
        gameMeta.setDisplayName(ChatColor.LIGHT_PURPLE + "Interacción con el juego");
        gameMeta.setLore(Arrays.asList(ChatColor.GRAY + "¡Próximamente!"));
        game.setItemMeta(gameMeta);
        menu.setItem(12, game);

        // Reportar jugador
        ItemStack report = new ItemStack(Material.PAPER);
        ItemMeta reportMeta = report.getItemMeta();
        reportMeta.setDisplayName(ChatColor.RED + "Reportar jugador");
        reportMeta.setLore(Arrays.asList(ChatColor.GRAY + "Haz clic para abrir el menú de reportes."));
        report.setItemMeta(reportMeta);
        menu.setItem(14, report);

        // Estadísticas
        ItemStack stats = new ItemStack(Material.CLOCK);
        ItemMeta statsMeta = stats.getItemMeta();
        statsMeta.setDisplayName(ChatColor.GOLD + "Estadísticas del servidor");
        statsMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Días jugados: " + daysPlayed
        ));
        stats.setItemMeta(statsMeta);
        menu.setItem(16, stats);

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(ChatColor.DARK_PURPLE + "Menú Principal")) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) e.getWhoClicked();
        switch (e.getCurrentItem().getType()) {
            case PAPER:
                Bukkit.dispatchCommand(player, "deathscape reportar");
                break;
            default:
                break;
        }
    }
}

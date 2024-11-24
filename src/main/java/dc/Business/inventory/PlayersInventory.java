package dc.Business.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;

public abstract class PlayersInventory extends InventorySystem {

    public PlayersInventory(String title) {
        super(title);
    }

    @Override
    protected void generateInventory() {
        items.clear();

        List<String> playersList = getPlayersList();
        for (String playerName : playersList) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
                skullMeta.setDisplayName(getPlayerDisplayName(playerName));
                playerHead.setItemMeta(skullMeta);
                items.add(playerHead);
            }
        }
    }

    protected abstract List<String> getPlayersList();  // Método para obtener la lista de jugadores

    protected abstract String getPlayerDisplayName(String playerName);  // Método para definir el nombre a mostrar

    @Override
    protected void onItemClicked(Player player, ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            String playerName = Objects.requireNonNull(((SkullMeta) Objects.requireNonNull(item.getItemMeta())).getOwningPlayer()).getName();
            onPlayerSelected(player, playerName);
            player.closeInventory();
        }
    }

    protected abstract void onPlayerSelected(Player player, String playerName);  // Acción específica al seleccionar un jugador
}

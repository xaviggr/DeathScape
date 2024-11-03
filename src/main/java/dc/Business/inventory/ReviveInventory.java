package dc.Business.inventory;

import dc.Persistence.player.PlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;

public class ReviveInventory extends InventorySystem {

    public ReviveInventory() {
        super("Revivir Jugador");
    }

    @Override
    protected void onItemClicked(Player player, ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            String playerName = Objects.requireNonNull(((SkullMeta) Objects.requireNonNull(item.getItemMeta())).getOwningPlayer()).getName();
            revivePlayer(player, playerName);
            player.closeInventory();
        }
    }

    @Override
    protected void generateInventory() {
        // Limpiar la lista de items para evitar acumulación
        items.clear();  // Agrega esta línea

        List<String> deadPlayers = PlayerDatabase.getDeadPlayers();
        // Convertir cada jugador muerto a una cabeza de jugador y agregarlo a los items
        for (String playerName : deadPlayers) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            assert skullMeta != null;
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            skullMeta.setDisplayName("Revivir a " + playerName);
            playerHead.setItemMeta(skullMeta);

            items.add(playerHead);
        }
    }

    private void revivePlayer(Player reviver, String playerName) {
        // Lógica para revivir al jugador
        reviver.sendMessage("Has revivido a " + playerName + " exitosamente.");
        // Implementa la resurrección aquí
    }
}

package dc.Business.inventory;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;

/**
 * Abstract class representing a paginated inventory system for displaying and interacting with players.
 * Extends the {@link InventorySystem} to provide player-specific functionality.
 */
public abstract class PlayersInventory extends InventorySystem {

    /**
     * Constructs a PlayersInventory with a specified title.
     *
     * @param title The title of the inventory.
     */
    public PlayersInventory(String title) {
        super(title);
    }

    /**
     * Generates the inventory content by creating player heads for each player in the list.
     * Each player head represents a player and can be clicked for interaction.
     */
    @Override
    protected void generateInventory() {
        items.clear();

        List<String> playersList = getPlayersList();
        for (String playerName : playersList) {
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName)); // Set the player head's owner
                skullMeta.setDisplayName(getPlayerDisplayName(playerName)); // Set the display name
                playerHead.setItemMeta(skullMeta);
                items.add(playerHead);
            }
        }
    }

    /**
     * Defines the behavior when a player clicks on an item in the inventory.
     * If the clicked item is a player head, retrieves the player name and calls {@link #onPlayerSelected(Player, String)}.
     *
     * @param player The player who clicked the item.
     * @param item   The clicked item.
     */
    @Override
    protected void onItemClicked(Player player, ItemStack item) {
        if (item.getType() == Material.PLAYER_HEAD) {
            String playerName = Objects.requireNonNull(
                    ((SkullMeta) Objects.requireNonNull(item.getItemMeta())).getOwningPlayer()
            ).getName();
            onPlayerSelected(player, playerName);
            player.closeInventory();
        }
    }

    /**
     * Abstract method to retrieve the list of players to display in the inventory.
     * Must be implemented by subclasses to provide the desired list of players.
     *
     * @return A list of player names to be displayed in the inventory.
     */
    protected abstract List<String> getPlayersList();

    /**
     * Abstract method to define the display name for each player in the inventory.
     * Must be implemented by subclasses to provide custom display names.
     *
     * @param playerName The name of the player.
     * @return The display name to be shown on the player's head.
     */
    protected abstract String getPlayerDisplayName(String playerName);

    /**
     * Abstract method to define the action when a player is selected.
     * Must be implemented by subclasses to handle the selection event.
     *
     * @param player     The player interacting with the inventory.
     * @param playerName The name of the player selected.
     */
    protected abstract void onPlayerSelected(Player player, String playerName);
}

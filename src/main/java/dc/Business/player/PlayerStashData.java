package dc.Business.player;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PlayerStashData {
    private String playerName;
    private List<ItemStack> stashItems;

    public PlayerStashData(String playerName, List<ItemStack> stashItems) {
        this.playerName = playerName;
        this.stashItems = stashItems;
    }

    public String getPlayerName() {
        return playerName;
    }

    public List<ItemStack> getStashItems() {
        return stashItems;
    }
}
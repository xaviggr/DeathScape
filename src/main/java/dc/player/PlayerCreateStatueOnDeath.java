package dc.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.Rotatable;
import org.bukkit.entity.Player;

public class PlayerCreateStatueOnDeath {

    public static void Create(Player player) {
        Location l = player.getEyeLocation().clone();
        if (l.getY() < 3) {
            l.setY(3);
        }
        Block skullBlock = l.getBlock();
        skullBlock.setType(Material.PLAYER_HEAD);

        Skull skullState = (Skull) skullBlock.getState();
        skullState.setOwningPlayer(player);
        skullState.update();

        Rotatable rotatable = (Rotatable) skullBlock.getBlockData();
        rotatable.setRotation(player.getFacing());
        skullBlock.setBlockData(rotatable);

        skullBlock.getRelative(BlockFace.DOWN).setType(Material.NETHER_BRICK_FENCE);
        skullBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
    }
}

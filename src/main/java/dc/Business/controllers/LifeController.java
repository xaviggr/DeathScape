package dc.Business.controllers;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class LifeController {

    private final HashMap<UUID, Integer> lives = new HashMap<>();
    private final JavaPlugin plugin;


    public LifeController(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public int getLives(UUID uuid) {
        return lives.getOrDefault(uuid, 1);
    }

    public void setLives(UUID uuid, int amount) {
        lives.put(uuid, amount);
    }

    public void addLives(UUID uuid, int amount) {
        setLives(uuid, getLives(uuid) + amount);
    }

    public void subtractLife(Player player) {
        UUID uuid = player.getUniqueId();
        int current = getLives(uuid) - 1;

        setLives(uuid, current);
        if (current < 1) {
            // Banear al jugador
            Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "Sin vidas restantes", null, null);
            player.kickPlayer("Has perdido todas tus vidas.");
        }
    }

    public void revivePlayer(OfflinePlayer player) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(player.getName());
        setLives(player.getUniqueId(), 1);
    }
}

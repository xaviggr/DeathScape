package dc.Business.controllers;

import dc.DeathScape;
import dc.utils.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonController {

    private final DeathScape plugin;
    private final PlayerController playerController;

    // Coordenadas de posibles spawns
    private static final List<Location> SPAWN_LOCATIONS = new ArrayList<>();

    // Coordenadas de los cofres
    private static final List<Location> CHEST_LOCATIONS = new ArrayList<>();

    private final Random random = new Random();

    static {
        World riftWorld = Bukkit.getWorld("world_minecraft_rift");
        if (riftWorld != null) {
            // Spawns
            SPAWN_LOCATIONS.add(new Location(riftWorld, 488, 150, -39));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 410, 149, -2));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 401, 149, 44));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 492, 149, 13));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 461, 141, 140));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 470, 141, 239));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 377, 135, 271));
            SPAWN_LOCATIONS.add(new Location(riftWorld, 344, 135, 232));

            // Cofres originales
            CHEST_LOCATIONS.add(new Location(riftWorld, 322, 135, 270));
            CHEST_LOCATIONS.add(new Location(riftWorld, 341, 135, 264));
            CHEST_LOCATIONS.add(new Location(riftWorld, 358, 135, 275));
            CHEST_LOCATIONS.add(new Location(riftWorld, 372, 135, 273));
            CHEST_LOCATIONS.add(new Location(riftWorld, 385, 135, 268));
            CHEST_LOCATIONS.add(new Location(riftWorld, 374, 136, 266));
            CHEST_LOCATIONS.add(new Location(riftWorld, 362, 137, 256));
            CHEST_LOCATIONS.add(new Location(riftWorld, 346, 135, 260));
            CHEST_LOCATIONS.add(new Location(riftWorld, 344, 137, 235));
            CHEST_LOCATIONS.add(new Location(riftWorld, 365, 136, 241));
            CHEST_LOCATIONS.add(new Location(riftWorld, 369, 136, 233));
            CHEST_LOCATIONS.add(new Location(riftWorld, 395, 135, 232));
            CHEST_LOCATIONS.add(new Location(riftWorld, 391, 135, 251));
            CHEST_LOCATIONS.add(new Location(riftWorld, 378, 136, 256));
            CHEST_LOCATIONS.add(new Location(riftWorld, 364, 135, 251));
            CHEST_LOCATIONS.add(new Location(riftWorld, 384, 135, 263));
            CHEST_LOCATIONS.add(new Location(riftWorld, 421, 141, 243));
            CHEST_LOCATIONS.add(new Location(riftWorld, 430, 141, 250));
            CHEST_LOCATIONS.add(new Location(riftWorld, 427, 142, 234));
            CHEST_LOCATIONS.add(new Location(riftWorld, 426, 141, 216));
            CHEST_LOCATIONS.add(new Location(riftWorld, 468, 142, 204));
            CHEST_LOCATIONS.add(new Location(riftWorld, 440, 141, 186));
            CHEST_LOCATIONS.add(new Location(riftWorld, 453, 141, 179));
            CHEST_LOCATIONS.add(new Location(riftWorld, 484, 141, 230));
            CHEST_LOCATIONS.add(new Location(riftWorld, 430, 149, 53));
            CHEST_LOCATIONS.add(new Location(riftWorld, 442, 149, 39));
            CHEST_LOCATIONS.add(new Location(riftWorld, 429, 149, 9));
            CHEST_LOCATIONS.add(new Location(riftWorld, 466, 149, 15));
            CHEST_LOCATIONS.add(new Location(riftWorld, 490, 149, -8));
            CHEST_LOCATIONS.add(new Location(riftWorld, 429, 149, -1));
            CHEST_LOCATIONS.add(new Location(riftWorld, 406, 150, 70));

            // Nuevas coordenadas de cofres
            CHEST_LOCATIONS.add(new Location(riftWorld, 421, 149, 86));
            CHEST_LOCATIONS.add(new Location(riftWorld, 427, 149, 88));
            CHEST_LOCATIONS.add(new Location(riftWorld, 425, 152, 97));
            CHEST_LOCATIONS.add(new Location(riftWorld, 447, 151, 72));
            CHEST_LOCATIONS.add(new Location(riftWorld, 399, 149, 42));
            CHEST_LOCATIONS.add(new Location(riftWorld, 429, 149, 36));
            CHEST_LOCATIONS.add(new Location(riftWorld, 412, 151, 39));
            CHEST_LOCATIONS.add(new Location(riftWorld, 404, 149, 17));
            CHEST_LOCATIONS.add(new Location(riftWorld, 478, 141, 208));
            CHEST_LOCATIONS.add(new Location(riftWorld, 455, 141, 164));
            CHEST_LOCATIONS.add(new Location(riftWorld, 423, 149, 105));
            CHEST_LOCATIONS.add(new Location(riftWorld, 434, 149, 116));
            CHEST_LOCATIONS.add(new Location(riftWorld, 414, 149, 108));
            CHEST_LOCATIONS.add(new Location(riftWorld, 414, 149, 113));
            CHEST_LOCATIONS.add(new Location(riftWorld, 492, 149, -17));
            CHEST_LOCATIONS.add(new Location(riftWorld, 479, 150, 15));
            CHEST_LOCATIONS.add(new Location(riftWorld, 493, 149, 7));
            CHEST_LOCATIONS.add(new Location(riftWorld, 494, 150, 22));
            CHEST_LOCATIONS.add(new Location(riftWorld, 487, 149, 46));
            CHEST_LOCATIONS.add(new Location(riftWorld, 474, 149, 72));
            CHEST_LOCATIONS.add(new Location(riftWorld, 461, 152, 56));
            CHEST_LOCATIONS.add(new Location(riftWorld, 461, 152, 54));
            CHEST_LOCATIONS.add(new Location(riftWorld, 439, 149, 69));
            CHEST_LOCATIONS.add(new Location(riftWorld, 430, 149, 68));
            CHEST_LOCATIONS.add(new Location(riftWorld, 414, 149, 66));
            CHEST_LOCATIONS.add(new Location(riftWorld, 398, 149, 78));
            CHEST_LOCATIONS.add(new Location(riftWorld, 409, 149, 87));
            CHEST_LOCATIONS.add(new Location(riftWorld, 415, 149, 86));
            CHEST_LOCATIONS.add(new Location(riftWorld, 480, 149, 111));
            CHEST_LOCATIONS.add(new Location(riftWorld, 487, 149, 107));
            CHEST_LOCATIONS.add(new Location(riftWorld, 469, 149, 96));
            CHEST_LOCATIONS.add(new Location(riftWorld, 483, 149, 88));
            CHEST_LOCATIONS.add(new Location(riftWorld, 489, 149, 78));
            CHEST_LOCATIONS.add(new Location(riftWorld, 487, 149, 45));
            CHEST_LOCATIONS.add(new Location(riftWorld, 438, 141, 137));
            CHEST_LOCATIONS.add(new Location(riftWorld, 459, 142, 141));
            CHEST_LOCATIONS.add(new Location(riftWorld, 474, 142, 133));
            CHEST_LOCATIONS.add(new Location(riftWorld, 479, 141, 156));
            CHEST_LOCATIONS.add(new Location(riftWorld, 478, 141, 155));
            CHEST_LOCATIONS.add(new Location(riftWorld, 473, 141, 189));
            CHEST_LOCATIONS.add(new Location(riftWorld, 437, 144, 204));
            CHEST_LOCATIONS.add(new Location(riftWorld, 447, 141, 213));
            CHEST_LOCATIONS.add(new Location(riftWorld, 461, 141, 225));
            CHEST_LOCATIONS.add(new Location(riftWorld, 403, 150, 4));
            CHEST_LOCATIONS.add(new Location(riftWorld, 408, 149, -5));
            CHEST_LOCATIONS.add(new Location(riftWorld, 450, 150, 0));
            CHEST_LOCATIONS.add(new Location(riftWorld, 424, 149, -13));
            CHEST_LOCATIONS.add(new Location(riftWorld, 458, 149, -18));
            CHEST_LOCATIONS.add(new Location(riftWorld, 478, 149, -14));
        }
    }

    public DungeonController(DeathScape deathScape, PlayerController playerController) {
        this.plugin = deathScape;
        this.playerController = playerController;
        init();
    }

    private void init() {
        clearDungeonChests();
        fillChestsWithLoot();
    }

    /**
     * Encuentra una posición de spawn válida en las dungeons.
     * Verifica que no haya jugadores cerca antes de devolver la posición.
     *
     * @param player El jugador que intenta entrar.
     * @return Una ubicación válida de spawn o null si no hay disponibles.
     */
    public Location getValidSpawnLocation(Player player) {
        for (Location spawn : SPAWN_LOCATIONS) {
            if (isLocationSafe(spawn, player)) {
                return spawn;
            }
        }
        return null; // No hay ubicaciones válidas disponibles
    }

    /**
     * Comprueba si una ubicación es segura para spawnear.
     *
     * @param location La ubicación a verificar.
     * @param player   El jugador que intenta entrar.
     * @return true si no hay jugadores cerca, false en caso contrario.
     */
    private boolean isLocationSafe(Location location, Player player) {
        int radius = 10; // Radio de seguridad (puedes ajustar este valor)
        World world = location.getWorld();

        if (world == null) return false;

        // Comprueba si hay otros jugadores en el radio de seguridad
        for (Player nearbyPlayer : world.getPlayers()) {
            if (!nearbyPlayer.equals(player) && nearbyPlayer.getLocation().distance(location) <= radius) {
                return false; // Hay un jugador cerca, no es seguro
            }
        }
        return true; // No hay jugadores cerca, es seguro
    }

    /**
     * Teletransporta al jugador a una ubicación válida de spawn en las dungeons.
     *
     * @param player El jugador a teletransportar.
     */
    public void teleportPlayerToDungeon(Player player) {
        Location spawnLocation = getValidSpawnLocation(player);

        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            Message.sendTitleToPlayer(player,ChatColor.GOLD + "¡Bienvenido a la dungeon!", ChatColor.RED + "¡Prepárate para la batalla!", 10, 40, 10);
            Message.sendMessageAllPlayers(player.getName() + " ha entrado a las mazmorras.", ChatColor.BLUE);
        } else {
            Message.sendMessage(player, "No hay ubicaciones de spawn disponibles en este momento.", ChatColor.RED);
        }
    }

    /**
     * Rellena los cofres con loot aleatorio.
     */
    private void fillChestsWithLoot() {
        for (Location chestLocation : CHEST_LOCATIONS) {
            Block block = chestLocation.getBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();

                // Solo rellena cofres vacíos
                if (chest.getBlockInventory().isEmpty()) {
                    chest.getBlockInventory().setContents(generateRandomLoot());
                }
            }
        }
    }

    /**
     * Clear all the chests in the dungeon.
     */
    public void clearDungeonChests() {
        for (Location chestLocation : CHEST_LOCATIONS) {
            Block block = chestLocation.getBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                chest.getBlockInventory().clear();
            }
        }
    }

    /**
     * Genera loot aleatorio para un cofre.
     *
     * @return Un array de ItemStack con ítems aleatorios.
     */
    private ItemStack[] generateRandomLoot() {
        ItemStack[] loot = new ItemStack[27]; // Tamaño estándar de un cofre

        for (int i = 0; i < loot.length; i++) {
            double randomValue = random.nextDouble();

            // Probabilidad del 40% de que no haya nada
            if (randomValue < 0.4) {
                loot[i] = null; // Slot vacío

                // Probabilidad del 25% para ítems comunes
            } else if (randomValue < 0.65) { // 40% + 25% = 65%
                loot[i] = ItemsController.generateCommonItem();

                // Probabilidad del 15% para ítems poco comunes
            } else if (randomValue < 0.8) { // 65% + 15% = 80%
                loot[i] = ItemsController.generateUncommonItem();

                // Probabilidad del 10% para ítems raros
            } else if (randomValue < 0.9) { // 80% + 10% = 90%
                loot[i] = ItemsController.generateRareItem();

                // Probabilidad del 6% para ítems épicos
            } else if (randomValue < 0.96) { // 90% + 6% = 96%
                loot[i] = ItemsController.generateEpicItem();

                // Probabilidad del 3.99% para ítems legendarios
            } else if (randomValue < 0.9999) { // 96% + 3.99% = 99.99%
                loot[i] = ItemsController.generateLegendaryItem();

                // Probabilidad del 0.1% para ítems únicos
            } else { // 0.1% restante
                loot[i] = ItemsController.generateUniqueItem();
            }
        }
        return loot;
    }
}
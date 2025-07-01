package dc.Business.listeners.Player;

import dc.DeathScape;
import dc.Business.controllers.ItemsController;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;

public class PlayerCraftListener implements Listener {

    private final DeathScape plugin;
    private final ItemsController itemsController;

    public PlayerCraftListener(DeathScape plugin, ItemsController itemsController) {
        this.plugin = plugin;
        this.itemsController = itemsController;
    }

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        ItemStack[] matrix = inv.getMatrix();

        if (matrix.length != 9) return;

        ItemStack result = getCustomCraftingResult(matrix);
        inv.setResult(result);
    }

    private ItemStack getCustomCraftingResult(ItemStack[] matrix) {
        // --- DeathScape Totem Recipe ---
        if (
                isNetherite(matrix[0]) && isMejora(matrix[1]) && isNetherite(matrix[2]) &&
                        isMejora(matrix[3]) && isVanillaTotem(matrix[4]) && isMejora(matrix[5]) &&
                        isNetherite(matrix[6]) && isNotchApple(matrix[7]) && isNetherite(matrix[8])
        ) {
            return itemsController.generateCustomTotem("deathscape");
        }

        // --- Totem de Salto Recipe ---
        if (
                isEmpty(matrix[0]) && isRabbitFoot(matrix[1]) && isEmpty(matrix[2]) &&
                        isMejora(matrix[3]) && isVanillaTotem(matrix[4]) && isMejora(matrix[5]) &&
                        isEmpty(matrix[6]) && isRabbitFoot(matrix[7]) && isEmpty(matrix[8])
        ) {
            return itemsController.generateCustomTotem("jump");
        }

        // --- Totem de Explosión Recipe ---
        if (
                isEmpty(matrix[0]) && isGunpowder(matrix[1]) && isEmpty(matrix[2]) &&
                        isMejora(matrix[3]) && isVanillaTotem(matrix[4]) && isMejora(matrix[5]) &&
                        isEmpty(matrix[6]) && isGunpowder(matrix[7]) && isEmpty(matrix[8])
        ) {
            return itemsController.generateCustomTotem("explosion");
        }

        // --- Néctar del Guardián Recipe ---
        if (
                isEmpty(matrix[0]) && isFeather(matrix[1]) && isEmpty(matrix[2]) &&
                        isMejora(matrix[3]) && isWaterBottle(matrix[4]) && isMejora(matrix[5]) &&
                        isEmpty(matrix[6]) && isFeather(matrix[7]) && isEmpty(matrix[8])
        ) {
            return itemsController.generateCustomUtilityItem("nectar");
        }

        // --- Pergamino Recipe ---
        if (
                isNetherite(matrix[0]) && isDeathscapeTotem(matrix[1]) && isNetherite(matrix[2]) &&
                        isMejora(matrix[3]) && isPaper(matrix[4]) && isMejora(matrix[5]) &&
                        isNetherite(matrix[6]) && isNotchApple(matrix[7]) && isNetherite(matrix[8])
        ) {
            return itemsController.generateCustomUtilityItem("retiro");
        }

        // --- Corazón de Resurrección Recipe ---
        if (
                isEmpty(matrix[0]) && isEmpty(matrix[1]) && isEmpty(matrix[2]) &&
                        isHeartPlat(matrix[3]) && isHeartFire(matrix[4]) && isHeartIce(matrix[5]) &&
                        isEmpty(matrix[6]) && isEmpty(matrix[7]) && isEmpty(matrix[8])
        ) {
            return itemsController.generateCustomUtilityItem("res");
        }

        return null; // No crafteo válido
    }

    // --- Verificadores reutilizables ---

    private boolean isMejora(ItemStack item) {
        if (item == null || item.getType() != Material.PAPER) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        NamespacedKey key = new NamespacedKey(plugin, "CustomItemType");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)
                && "Mejora".equals(meta.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    private boolean isNetherite(ItemStack item) {
        return item != null && item.getType() == Material.NETHERITE_INGOT;
    }

    private boolean isVanillaTotem(ItemStack item) {
        return item != null && item.getType() == Material.TOTEM_OF_UNDYING;
    }

    private boolean isNotchApple(ItemStack item) {
        return item != null && item.getType() == Material.ENCHANTED_GOLDEN_APPLE;
    }

    private boolean isFeather(ItemStack item) {
        return item != null && item.getType() == Material.FEATHER;
    }

    private boolean isWaterBottle(ItemStack item) {
        if (item == null || item.getType() != Material.POTION) return false;
        if (!(item.getItemMeta() instanceof PotionMeta meta)) return false;
        return meta.getBasePotionData().getType() == PotionType.WATER;
    }

    private boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    private boolean isRabbitFoot(ItemStack item) {
        return item != null && item.getType() == Material.RABBIT_FOOT;
    }

    private boolean isGunpowder(ItemStack item) {
        return item != null && item.getType() == Material.GUNPOWDER;
    }

    private boolean isPaper(ItemStack item) {
        return item != null && item.getType() == Material.PAPER;
    }

    private boolean isDeathscapeTotem(ItemStack item) {
        if (item == null || item.getType() != Material.TOTEM_OF_UNDYING) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey key = new NamespacedKey(plugin, "TotemType");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING) &&
                "deathscape".equalsIgnoreCase(meta.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    private boolean isCustomHeart(ItemStack item, String type) {
        if (item == null || item.getType() != Material.PAPER) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        NamespacedKey key = new NamespacedKey(plugin, "CustomItemType");
        return meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)
                && type.equalsIgnoreCase(meta.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }

    private boolean isHeartPlat(ItemStack item) {
        return isCustomHeart(item, "Plat");
    }

    private boolean isHeartFire(ItemStack item) {
        return isCustomHeart(item, "Fire");
    }

    private boolean isHeartIce(ItemStack item) {
        return isCustomHeart(item, "Ice");
    }

}

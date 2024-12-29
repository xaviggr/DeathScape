package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemsController {

    private final DeathScape plugin;
    private static final Random random = new Random();

    // Listas de ítems por categoría
    private static final Material[] COMMON_ITEMS = {
            Material.ROTTEN_FLESH, Material.STRING, Material.SPIDER_EYE, Material.BONE
    };

    private static final Material[] UNCOMMON_ITEMS = {
            Material.IRON_INGOT, Material.GOLD_INGOT, Material.COOKED_BEEF, Material.EXPERIENCE_BOTTLE,
            Material.SHIELD
    };

    private static final Material[] RARE_ITEMS = {
            Material.DIAMOND, Material.EMERALD, Material.TOTEM_OF_UNDYING,
            Material.NETHERITE_SCRAP, Material.ENCHANTED_GOLDEN_APPLE
    };

    private static final Material[] EPIC_ITEMS = {
            Material.NETHERITE_SWORD, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_PICKAXE,
            Material.BOW
    };

    private static final Material[] LEGENDARY_ITEMS = {
            Material.NETHERITE_SWORD, Material.NETHERITE_CHESTPLATE, Material.BEACON,
            Material.ELYTRA, Material.DRAGON_HEAD
    };

    private static final Material[] UNIQUE_ITEMS = {
            Material.ENCHANTED_BOOK, Material.NETHER_STAR, Material.END_CRYSTAL
    };

    public ItemsController(DeathScape deathScape) {
        this.plugin = deathScape;
    }

    /**
     * Genera un ítem de la categoría "Común".
     *
     * @return Un ItemStack de ítems comunes.
     */
    public static ItemStack generateCommonItem() {
        Material randomMaterial = COMMON_ITEMS[random.nextInt(COMMON_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, random.nextInt(8) + 1); // De 1 a 8 unidades

        // Personaliza el ítem con lore
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GRAY + "Ítem Común");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "Un objeto simple y abundante.");
            lore.add(ChatColor.DARK_GRAY + "Útil para principiantes o en emergencias.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Genera un ítem de la categoría "Poco Común".
     *
     * @return Un ItemStack de ítems poco comunes.
     */
    public static ItemStack generateUncommonItem() {
        Material randomMaterial = UNCOMMON_ITEMS[random.nextInt(UNCOMMON_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, random.nextInt(5) + 1); // De 1 a 5 unidades

        // Añade encantamientos específicos para escudos
        if (randomMaterial == Material.SHIELD) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GREEN + "Escudo Reforzado");
                meta.addEnchant(Enchantment.DURABILITY, 2, true); // Irrompibilidad II
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.DARK_GREEN + "Un escudo poco común.");
                lore.add(ChatColor.DARK_GREEN + "Ideal para resistir ataques leves.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    /**
     * Genera un ítem de la categoría "Raro".
     *
     * @return Un ItemStack de ítems raros.
     */
    public static ItemStack generateRareItem() {
        Material randomMaterial = RARE_ITEMS[random.nextInt(RARE_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, random.nextInt(2) + 1); // De 1 a 2 unidades

        // Personaliza el ítem con lore
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.BLUE + "Ítem Raro");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_BLUE + "Un objeto valioso y codiciado.");
            lore.add(ChatColor.DARK_BLUE + "Los aventureros lo buscan constantemente.");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Genera un ítem de la categoría "Épico".
     *
     * @return Un ItemStack de ítems épicos.
     */
    public static ItemStack generateEpicItem() {
        Material randomMaterial = EPIC_ITEMS[random.nextInt(EPIC_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, 1);

        // Añade encantamientos relevantes
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "Ítem Épico");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.LIGHT_PURPLE + "Un objeto de gran poder.");
            lore.add(ChatColor.LIGHT_PURPLE + "Forjado para auténticos héroes.");
            meta.setLore(lore);

            if (randomMaterial == Material.NETHERITE_SWORD) {
                meta.addEnchant(Enchantment.DAMAGE_ALL, 5, true); // Filo V
                meta.addEnchant(Enchantment.FIRE_ASPECT, 2, true); // Aspecto Ígneo II
            } else if (randomMaterial == Material.NETHERITE_PICKAXE) {
                meta.addEnchant(Enchantment.DIG_SPEED, 5, true); // Eficiencia V
                meta.addEnchant(Enchantment.DURABILITY, 3, true); // Irrompibilidad III
            } else if (randomMaterial == Material.BOW) {
                meta.addEnchant(Enchantment.ARROW_DAMAGE, 5, true); // Poder V
                meta.addEnchant(Enchantment.ARROW_FIRE, 1, true); // Fuego
            } else if (randomMaterial == Material.NETHERITE_CHESTPLATE) {
                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true); // Protección IV
                meta.addEnchant(Enchantment.THORNS, 3, true); // Espinas III
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Genera un ítem de la categoría "Legendario".
     *
     * @return Un ItemStack de ítems legendarios.
     */
    public static ItemStack generateLegendaryItem() {
        Material randomMaterial = LEGENDARY_ITEMS[random.nextInt(LEGENDARY_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, 1);

        // Personaliza el ítem con encantamientos y lore avanzados
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Ítem Legendario");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_PURPLE + "Un artefacto único en su clase.");
            lore.add(ChatColor.DARK_RED + "Solo un verdadero campeón puede usarlo.");
            meta.setLore(lore);

            if (randomMaterial == Material.NETHERITE_SWORD) {
                meta.addEnchant(Enchantment.DAMAGE_ALL, 7, true); // Filo VII
                meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true); // Aspecto Ígneo III
                meta.addEnchant(Enchantment.DURABILITY, 5, true); // Irrompibilidad V
            } else if (randomMaterial == Material.ELYTRA) {
                meta.addEnchant(Enchantment.DURABILITY, 5, true); // Irrompibilidad V
                lore.add(ChatColor.LIGHT_PURPLE + "Permite volar como un dragón.");
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Genera un ítem de la categoría "Único" con probabilidades extremadamente bajas.
     *
     * @return Un ItemStack de ítems únicos.
     */
    public static ItemStack generateUniqueItem() {
        Material randomMaterial = UNIQUE_ITEMS[random.nextInt(UNIQUE_ITEMS.length)];
        ItemStack item = new ItemStack(randomMaterial, 1);

        // Personaliza el ítem con encantamientos y lore únicos
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ítem Único");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.AQUA + "Un objeto legendario de otro mundo.");
            lore.add(ChatColor.GOLD + "¡La historia lo recuerda como inmortal!");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }
}
package dc.Business.controllers;

import dc.DeathScape;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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
            if (randomMaterial == Material.ENCHANTED_BOOK) {
                // Configurar encantamientos superchetados para el libro
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Libro Único");
                EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) meta;
                bookMeta.addStoredEnchant(Enchantment.DAMAGE_ALL, 10, true); // Filo Nivel 10
                bookMeta.addStoredEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 10, true); // Protección Nivel 10
                bookMeta.addStoredEnchant(Enchantment.MENDING, 1, true); // Reparación
                bookMeta.addStoredEnchant(Enchantment.SWEEPING_EDGE, 5, true); // Barrido Nivel 5
                item.setItemMeta(bookMeta);
            } else {
                // Personalizar lore para otros ítems únicos
                meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Ítem Único");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.AQUA + "Un objeto legendario de otro mundo.");
                lore.add(ChatColor.GOLD + "¡La historia lo recuerda como inmortal!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    /**
     * Genera un totem personalizado con NBT TotemType.
     *
     * @param type El tipo de totem (por ejemplo "Jump", "Explosion").
     * @return Un ItemStack representando el totem personalizado.
     */
    public ItemStack generateCustomTotem(String type) {
        ItemStack item;

        switch (type.toLowerCase()) {
            case "jump" -> {
                item = new ItemStack(Material.TOTEM_OF_UNDYING);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.BLUE + "Totem de Salto");
                meta.setCustomModelData(10);

                NamespacedKey key = new NamespacedKey(plugin, "TotemType");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Jump");

                meta.setLore(List.of(
                        ChatColor.GRAY + "Otorga salto potenciado al revivir.",
                        ChatColor.DARK_PURPLE + "'Creado por antiguos alquimistas del viento.'",
                        ChatColor.BLUE + "" + ChatColor.ITALIC + "Raro"
                ));
                item.setItemMeta(meta);
            }

            case "explosion" -> {
                item = new ItemStack(Material.TOTEM_OF_UNDYING);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_PURPLE + "Totem de Explosión");
                meta.setCustomModelData(11);

                NamespacedKey key = new NamespacedKey(plugin, "TotemType");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "Explosion");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Explota causando daño en área al revivir.",
                        ChatColor.DARK_PURPLE + "'Forjado con el alma de un dragón furioso.'",
                        ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Épico"
                ));
                item.setItemMeta(meta);
            }

            case "deathscape" -> {
                item = new ItemStack(Material.TOTEM_OF_UNDYING);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GOLD + "DeathScape Totem");
                meta.setCustomModelData(12);

                NamespacedKey key = new NamespacedKey(plugin, "TotemType");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "DeathScape");

                meta.setLore(List.of(
                        ChatColor.GRAY + "Teletransporta al usuario a un lugar seguro.",
                        ChatColor.DARK_PURPLE + "'Creado por los antiguos para escapar de la muerte misma.'",
                        ChatColor.GOLD + "" + ChatColor.ITALIC + "Legendario"
                ));
                item.setItemMeta(meta);
            }

            default -> throw new IllegalArgumentException("Tipo de totem no soportado: " + type);
        }

        return item;
    }

    public ItemStack generateCustomUtilityItem(String type) {
        ItemStack item;

        switch (type.toLowerCase()) {
            case "dash" -> {
                item = new ItemStack(Material.FEATHER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GOLD + "Propulsor");
                meta.setCustomModelData(10);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Dash");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Haz clic derecho para impulsarte hacia adelante.",
                        ChatColor.RED + "5 segundos de enfriamiento.",
                        ChatColor.DARK_PURPLE + "'Diseñado por el mensajero de los dioses.'",
                        ChatColor.GOLD + "" + ChatColor.ITALIC + "Legendario"
                ));
                item.setItemMeta(meta);
            }

            case "catalizador", "catalyst" -> {
                item = new ItemStack(Material.APPLE);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GREEN + "Nube de impulso");
                meta.setCustomModelData(20);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "JumpBoost");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Te lanza hacia arriba al consumirla.",
                        ChatColor.RED + "Se destruye tras el uso.",
                        ChatColor.DARK_PURPLE + "'Extraída del suspiro de una nube viviente.'",
                        ChatColor.GREEN + "" + ChatColor.ITALIC + "Poco común"
                ));
                item.setItemMeta(meta);
            }

            case "invisibilidad", "invisible" -> {
                item = new ItemStack(Material.APPLE);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_PURPLE + "Manzana de Invisibilidad Total");
                meta.setCustomModelData(21);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Invisibilidad");
                meta.setLore(List.of(
                        ChatColor.WHITE + "Te vuelve invisible durante 10 segundos.",
                        ChatColor.DARK_GRAY + "No pueden verte ni mobs ni jugadores.",
                        ChatColor.DARK_PURPLE + "'Solo los verdaderamente olvidados logran desaparecer así.'",
                        ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Épico"
                ));
                item.setItemMeta(meta);
            }

            case "beleño", "beleñonegro" -> {
                item = new ItemStack(Material.APPLE);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_RED + "Beleño Negro");
                meta.setCustomModelData(22);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Beleño");
                meta.setLore(List.of(
                        ChatColor.RED + "Efecto: +100% daño cuerpo a cuerpo",
                        ChatColor.GRAY + "pero recibes el doble de daño durante 10 segundos.",
                        ChatColor.DARK_RED + "Peligroso de usar, incluso para los expertos.",
                        ChatColor.DARK_PURPLE + "'Utilizado por los guerreros del caos en rituales prohibidos.'",
                        ChatColor.RED + "" + ChatColor.ITALIC + "Único"
                ));
                item.setItemMeta(meta);
            }

            case "retiro", "pergamino", "scroll" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GOLD + "Pergamino del Retiro");
                meta.setCustomModelData(23);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "ScrollReturn");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Te devuelve a tu último punto de respawn.",
                        ChatColor.RED + "Un solo uso.",
                        ChatColor.DARK_PURPLE + "'Escrito con tinta del río del tiempo.'",
                        ChatColor.GOLD + "" + ChatColor.ITALIC + "Legendario"
                ));
                item.setItemMeta(meta);
            }

            case "nectar", "guardian", "nectarguardian" -> {
                item = new ItemStack(Material.HONEY_BOTTLE);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.GREEN + "Néctar del Guardián");
                meta.setCustomModelData(24);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "NectarCaida");
                meta.setLore(List.of(
                        ChatColor.WHITE + "Inmunidad al daño por caída durante 60 segundos.",
                        ChatColor.DARK_GRAY + "Ideal para exploradores y acróbatas.",
                        ChatColor.DARK_PURPLE + "'Se dice que un guardián de las alturas lloró este néctar.'",
                        ChatColor.GREEN + "" + ChatColor.ITALIC + "Poco común"
                ));
                item.setItemMeta(meta);
            }

            case "mejora" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.DARK_PURPLE + "Mejora");
                meta.setCustomModelData(24);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Mejora");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Usado para mejorar ciertos objetos.",
                        ChatColor.DARK_PURPLE + "'Solo los artesanos bendecidos pueden activarlo.'",
                        ChatColor.GRAY + "" + ChatColor.ITALIC + "Común"
                ));
                item.setItemMeta(meta);
            }

            case "plat" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.BLUE + "Corazón de plata");
                meta.setCustomModelData(2);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Plat");
                meta.setLore(List.of(
                        ChatColor.WHITE + "Un peculiar corazón de plata.",
                        ChatColor.DARK_GRAY + "Al parecer... late.",
                        ChatColor.DARK_PURPLE + "'Un fragmento de luna atrapado en un suspiro.'",
                        ChatColor.ITALIC + "" + ChatColor.GRAY + "Común"
                ));
                item.setItemMeta(meta);
            }

            case "fire" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.RED + "Corazón de fuego");
                meta.setCustomModelData(3);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Fire");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Un corazón ardiente que emite calor real.",
                        ChatColor.DARK_PURPLE + "'El alma de una salamandra duerme en su interior.'",
                        ChatColor.BLUE + "" + ChatColor.ITALIC + "Raro"
                ));
                item.setItemMeta(meta);
            }

            case "ice" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.AQUA + "Corazón de hielo");
                meta.setCustomModelData(4);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Ice");
                meta.setLore(List.of(
                        ChatColor.WHITE + "Extremadamente frío al tacto.",
                        ChatColor.DARK_GRAY + "Congela incluso el alma.",
                        ChatColor.DARK_PURPLE + "'Nacido del susurro de una tormenta eterna.'",
                        ChatColor.AQUA + "" + ChatColor.ITALIC + "Raro"
                ));
                item.setItemMeta(meta);
            }

            case "res" -> {
                item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                assert meta != null;
                meta.setDisplayName(ChatColor.RED + "Corazón de resurrección");
                meta.setCustomModelData(5);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "CustomItemType"), PersistentDataType.STRING, "Res");
                meta.setLore(List.of(
                        ChatColor.GRAY + "Revive a un jugador caído.",
                        ChatColor.DARK_PURPLE + "'Un sacrificio necesario para devolver la vida.'",
                        ChatColor.RED + "" + ChatColor.ITALIC + "Único"
                ));
                item.setItemMeta(meta);
            }

            default -> throw new IllegalArgumentException("Tipo de objeto no soportado: " + type);
        }

        return item;
    }
}
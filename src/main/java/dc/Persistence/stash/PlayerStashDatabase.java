package dc.Persistence.stash;

import com.google.gson.reflect.TypeToken;
import dc.Persistence.stash.BukkitSerialization;
import dc.utils.GsonUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class PlayerStashDatabase {
    private static final File file = new File("plugins/DeathScape/PlayerStashData.json");
    private static final Type type = new TypeToken<Map<String, List<String>>>() {}.getType();

    private static Map<String, List<String>> data = new HashMap<>();

    public static void load() {
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            data = GsonUtils.getGson().fromJson(reader, type);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error loading PlayerStashData: " + e.getMessage());
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(file)) {
            GsonUtils.getGson().toJson(data, writer);
        } catch (Exception e) {
            Bukkit.getLogger().severe("Error saving stash data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<ItemStack> getStash(String playerName) {
        List<String> encoded = data.getOrDefault(playerName, Arrays.asList("", "", "", ""));
        List<ItemStack> stash = new ArrayList<>();
        for (String base64 : encoded) {
            if (base64 != null && !base64.isEmpty()) {
                stash.add(BukkitSerialization.itemFromBase64(base64));
            } else {
                stash.add(null);
            }
        }
        return stash;
    }

    public static void setStash(String playerName, List<ItemStack> stashItems) {
        List<String> encoded = new ArrayList<>();
        for (ItemStack item : stashItems) {
            encoded.add(item != null ? BukkitSerialization.itemToBase64(item) : "");
        }
        data.put(playerName, encoded);
        save();
    }
}

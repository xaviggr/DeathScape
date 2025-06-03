package dc.utils;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.Base64;

public class GsonUtils {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .setPrettyPrinting()
            .create();

    public static Gson getGson() {
        return gson;
    }

    public static class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {
        @Override
        public JsonElement serialize(ItemStack item, java.lang.reflect.Type type, JsonSerializationContext context) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(item);
                return new JsonPrimitive(Base64.getEncoder().encodeToString(baos.toByteArray()));
            } catch (IOException e) {
                e.printStackTrace();
                return JsonNull.INSTANCE;
            }
        }

        @Override
        public ItemStack deserialize(JsonElement json, java.lang.reflect.Type type, JsonDeserializationContext context) throws JsonParseException {
            byte[] data = Base64.getDecoder().decode(json.getAsString());
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (ItemStack) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}

package dc.Persistence.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dc.Business.groups.GroupData;
import dc.Business.player.PlayerData;
import dc.Persistence.groups.GroupDatabase;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerDatabase {

    private static String nameFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void setNameFile(String nameFile) {
        PlayerDatabase.nameFile = nameFile;
    }

    public static void initPlayerDatabase() {
        File file = new File(nameFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static JsonObject readJsonFile() {
        File file = new File(nameFile);
        if (!file.exists()) {
            initPlayerDatabase();
            return new JsonObject();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return GSON.fromJson(bufferedReader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(nameFile)) {
            fileWriter.write(GSON.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean addPlayerDataToDatabase(PlayerData playerData) {
        JsonObject jsonObject = readJsonFile();
        jsonObject.add(playerData.getName(), GSON.toJsonTree(playerData));
        writeJsonFile(jsonObject);
        PlayerEditDatabase.addPlayerToGroup(playerData.getName(), playerData.getGroup());
        return true;
    }


    public static PlayerData getPlayerDataFromDatabase(String playerName) {
        JsonObject jsonObject = readJsonFile();
        JsonObject playerObject = jsonObject.getAsJsonObject(playerName);

        if (playerObject != null) {
            return GSON.fromJson(playerObject, PlayerData.class);
        }
        return null;
    }

    public static List<String> getDeadPlayers() {
        List<String> deadPlayers = new ArrayList<>();
        JsonObject jsonObject = readJsonFile();

        jsonObject.entrySet().stream()
                .filter(entry -> entry.getValue().getAsJsonObject().get("isDead").getAsBoolean())
                .forEach(entry -> deadPlayers.add(entry.getKey()));

        return deadPlayers;
    }

    public static List<String> getAllPlayers() {
        return new ArrayList<>(readJsonFile().keySet());
    }

    public static void setPlayerGroup(String player, String group) {
        PlayerData playerData = getPlayerDataFromDatabase(player);
        if (playerData != null) {
            playerData.setGroup(group);
            addPlayerDataToDatabase(playerData);
        }
    }
}
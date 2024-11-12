package dc.Persistence.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerDatabase {

    private static String nameFile;

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
            StringBuilder jsonString = new StringBuilder();
            String linea;
            while ((linea = bufferedReader.readLine()) != null) {
                jsonString.append(linea);
            }
            return new Gson().fromJson(jsonString.toString(), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(nameFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean addPlayerDataToDatabase(PlayerData playerData) {
        JsonObject jsonObject = readJsonFile();
        JsonObject playerObject = new JsonObject();

        playerObject.addProperty("Name", playerData.getName());
        playerObject.addProperty("isDead", playerData.isDead());
        playerObject.addProperty("Deaths", playerData.getDeaths());
        playerObject.addProperty("IP", playerData.getHostAddress());
        playerObject.addProperty("TimePlayed", playerData.getTimePlayed());
        playerObject.addProperty("UUID", playerData.getUuid().toString());
        playerObject.addProperty("BanDate", playerData.getBanDate());
        playerObject.addProperty("BanTime", playerData.getBantime());
        playerObject.addProperty("Coords", playerData.getCoords());
        playerObject.addProperty("Points", playerData.getPoints());

        jsonObject.add(playerData.getName(), playerObject);
        writeJsonFile(jsonObject);
        return true;
    }

    public static PlayerData getPlayerDataFromDatabase(String playerName) {
        JsonObject jsonObject = readJsonFile();
        JsonObject playerObject = jsonObject.getAsJsonObject(playerName);

        if (playerObject != null) {
            return new PlayerData(
                    playerName,
                    playerObject.get("isDead").getAsBoolean(),
                    playerObject.get("Deaths").getAsInt(),
                    playerObject.get("IP").getAsString(),
                    playerObject.get("TimePlayed").getAsString(),
                    UUID.fromString(playerObject.get("UUID").getAsString()),
                    playerObject.get("BanDate").getAsString(),
                    playerObject.get("BanTime").getAsString(),
                    playerObject.get("Coords").getAsString(),
                    playerObject.get("Points").getAsInt()
            );
        }
        return null;
    }

    public static List<String> getDeadPlayers() {
        List<String> deadPlayers = new ArrayList<>();
        JsonObject jsonObject = readJsonFile();

        for (String playerName : jsonObject.keySet()) {
            if (jsonObject.getAsJsonObject(playerName).get("isDead").getAsBoolean()) {
                deadPlayers.add(playerName);
            }
        }
        return deadPlayers;
    }

    public static List<String> getAllPlayers() {
        JsonObject jsonObject = readJsonFile();

        return new ArrayList<>(jsonObject.keySet());
    }
}
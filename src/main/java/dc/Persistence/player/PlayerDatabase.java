package dc.Persistence.player;

import com.google.gson.*;
import dc.Business.player.PlayerData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A database class for managing player data.
 * Provides methods to read, write, and manipulate player information stored in a JSON file.
 */
public class PlayerDatabase {

    private static String nameFile; // Path to the file storing player data
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Sets the file path for the player database.
     *
     * @param nameFile The file path to store player data.
     */
    public static void setNameFile(String nameFile) {
        PlayerDatabase.nameFile = nameFile;
    }

    /**
     * Initializes the player database by creating the file if it doesn't exist.
     * The database is initialized with an empty JSON object.
     */
    public static void initPlayerDatabase() {
        File file = new File(nameFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}"); // Initialize with an empty JSON object
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the JSON file containing player data.
     *
     * @return A JsonObject containing all player data.
     */
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

    /**
     * Writes the given player data to the JSON file.
     *
     * @param jsonObject A JsonObject containing the updated player data.
     */
    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(nameFile)) {
            fileWriter.write(GSON.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds player data to the database.
     *
     * @param playerData The {@link PlayerData} object representing the player data to add.
     * @return true if the operation succeeds.
     */
    public static boolean addPlayerDataToDatabase(PlayerData playerData) {
        JsonObject jsonObject = readJsonFile();
        jsonObject.add(playerData.getName(), GSON.toJsonTree(playerData));
        writeJsonFile(jsonObject);
        PlayerEditDatabase.addPlayerToGroup(playerData.getName(), playerData.getGroup());
        return true;
    }

    /**
     * Retrieves player data from the database.
     *
     * @param playerName The name of the player to retrieve.
     * @return A {@link PlayerData} object if found, or null if not found.
     */
    public static PlayerData getPlayerDataFromDatabase(String playerName) {
        JsonObject jsonObject = readJsonFile();
        JsonObject playerObject = jsonObject.getAsJsonObject(playerName);

        if (playerObject != null) {
            return GSON.fromJson(playerObject, PlayerData.class);
        }
        return null;
    }

    /**
     * Retrieves a list of dead players from the database.
     *
     * @return A list of player names who are marked as dead.
     */
    public static List<String> getDeadPlayers() {
        List<String> deadPlayers = new ArrayList<>();
        JsonObject jsonObject = readJsonFile();

        jsonObject.entrySet().stream()
                .filter(entry -> entry.getValue().getAsJsonObject().get("isDead").getAsBoolean())
                .forEach(entry -> deadPlayers.add(entry.getKey()));

        return deadPlayers;
    }

    /**
     * Retrieves a list of all players from the database.
     *
     * @return A list of all player names.
     */
    public static List<String> getAllPlayers() {
        return new ArrayList<>(readJsonFile().keySet());
    }

    /**
     * Updates the group of a player in the database.
     *
     * @param player The name of the player whose group is to be updated.
     * @param group  The name of the new group.
     */
    public static void setPlayerGroup(String player, String group) {
        PlayerData playerData = getPlayerDataFromDatabase(player);
        if (playerData != null) {
            playerData.setGroup(group);
            addPlayerDataToDatabase(playerData);
        }
    }

    /**
     * Returns a list of PlayerData sorted by points from highest to lowest.
     *
     * @return List of PlayerData sorted by points.
     */
    public static List<PlayerData> getLeaderboard() {
        List<PlayerData> leaderboard = new ArrayList<>();
        JsonObject jsonObject = readJsonFile();

        jsonObject.entrySet().forEach(entry -> {
            PlayerData data = GSON.fromJson(entry.getValue(), PlayerData.class);
            leaderboard.add(data);
        });

        leaderboard.sort((a, b) -> Integer.compare(b.getPoints(), a.getPoints())); // Descending
        return leaderboard;
    }
}
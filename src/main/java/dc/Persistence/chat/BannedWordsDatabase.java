package dc.Persistence.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the management of banned words and muted users for the chat system.
 * This class provides methods to add, remove, and retrieve banned words and muted users
 * from a JSON-based database file.
 */
public class BannedWordsDatabase {

    private static String databaseFile; // Path to the JSON database file

    /**
     * Sets the path to the JSON database file.
     *
     * @param filename The path to the database file.
     */
    public static void setDatabaseFile(String filename) {
        databaseFile = filename;
    }

    /**
     * Initializes the database file. If the file does not exist, it creates a new one
     * with default JSON structure for banned words and muted users.
     */
    public static void initDatabase() {
        File file = new File(databaseFile);
        if (!file.exists()) {
            try {
                // Create the file if it does not exist
                file.createNewFile();

                // Create the initial JSON structure
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("bannedWords", new JsonArray());
                jsonObject.add("mutedUsers", new JsonArray());

                // Write the JSON structure to the file
                writeJsonFile(jsonObject);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the JSON database file and returns its contents as a `JsonObject`.
     *
     * @return The `JsonObject` representation of the database file.
     */
    private static JsonObject readJsonFile() {
        File file = new File(databaseFile);
        if (!file.exists()) {
            initDatabase();
            return new JsonObject();
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonString.append(line);
            }
            return new Gson().fromJson(jsonString.toString(), JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JsonObject();
    }

    /**
     * Writes a `JsonObject` to the database file.
     *
     * @param jsonObject The `JsonObject` to write to the file.
     */
    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(databaseFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ------------------- Banned Words Management -------------------

    /**
     * Retrieves the set of banned words from the database.
     *
     * @return A set of banned words.
     */
    public static Set<String> getBannedWords() {
        JsonObject jsonObject = readJsonFile();
        JsonArray bannedWordsArray = jsonObject.getAsJsonArray("bannedWords");
        Set<String> bannedWords = new HashSet<>();

        if (bannedWordsArray != null) {
            bannedWordsArray.forEach(word -> {
                String bannedWord = word.getAsString().trim();
                if (!bannedWord.isEmpty()) {
                    bannedWords.add(bannedWord);
                }
            });
        }
        return bannedWords;
    }

    /**
     * Adds a new banned word to the database.
     *
     * @param word The word to ban.
     */
    public static void addBannedWord(String word) {
        JsonObject jsonObject = readJsonFile();
        JsonArray bannedWordsArray = jsonObject.getAsJsonArray("bannedWords");

        if (bannedWordsArray == null) {
            bannedWordsArray = new JsonArray();
            jsonObject.add("bannedWords", bannedWordsArray);
        }

        bannedWordsArray.add(word);
        writeJsonFile(jsonObject);
    }

    /**
     * Removes a banned word from the database.
     *
     * @param word The word to unban.
     */
    public static void removeBannedWord(String word) {
        JsonObject jsonObject = readJsonFile();
        JsonArray bannedWordsArray = jsonObject.getAsJsonArray("bannedWords");

        if (bannedWordsArray != null) {
            JsonArray newBannedWordsArray = new JsonArray();
            for (int i = 0; i < bannedWordsArray.size(); i++) {
                String bannedWord = bannedWordsArray.get(i).getAsString();
                if (!bannedWord.equalsIgnoreCase(word)) {
                    newBannedWordsArray.add(bannedWord);
                }
            }
            jsonObject.add("bannedWords", newBannedWordsArray);
            writeJsonFile(jsonObject);
        }
    }

    // ------------------- Muted Users Management -------------------

    /**
     * Retrieves the set of muted users from the database.
     *
     * @return A set of muted usernames.
     */
    public static Set<String> getMutedUsers() {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");
        Set<String> mutedUsers = new HashSet<>();

        if (mutedUsersArray != null) {
            mutedUsersArray.forEach(user -> {
                JsonObject mutedUserObject = user.getAsJsonObject();
                String mutedUser = mutedUserObject.get("username").getAsString().trim();
                if (!mutedUser.isEmpty()) {
                    mutedUsers.add(mutedUser);
                }
            });
        }
        return mutedUsers;
    }

    /**
     * Adds a user to the muted users list with a specified unmute time.
     *
     * @param username   The username to mute.
     * @param unmuteTime The time (in milliseconds) when the user will be unmuted.
     */
    public static void addMutedUser(String username, long unmuteTime) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray == null) {
            mutedUsersArray = new JsonArray();
            jsonObject.add("mutedUsers", mutedUsersArray);
        }

        JsonObject mutedUserObject = new JsonObject();
        mutedUserObject.addProperty("username", username);
        mutedUserObject.addProperty("unmuteTime", unmuteTime);

        mutedUsersArray.add(mutedUserObject);
        writeJsonFile(jsonObject);
    }

    /**
     * Removes a user from the muted users list.
     *
     * @param username The username to unmute.
     */
    public static void removeMutedUser(String username) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray != null) {
            JsonArray newMutedUsersArray = new JsonArray();
            for (int i = 0; i < mutedUsersArray.size(); i++) {
                JsonObject mutedUserObject = mutedUsersArray.get(i).getAsJsonObject();
                String mutedUser = mutedUserObject.get("username").getAsString();
                if (!mutedUser.equalsIgnoreCase(username)) {
                    newMutedUsersArray.add(mutedUserObject);
                }
            }
            jsonObject.add("mutedUsers", newMutedUsersArray);
            writeJsonFile(jsonObject);
        }
    }

    /**
     * Checks if a user is currently muted.
     * Automatically removes the user from the muted list if the mute duration has expired.
     *
     * @param username The username to check.
     * @return True if the user is muted, false otherwise.
     */
    public static boolean isUserMuted(String username) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray != null) {
            long currentTime = System.currentTimeMillis();
            JsonArray newMutedUsersArray = new JsonArray();

            for (int i = 0; i < mutedUsersArray.size(); i++) {
                JsonObject mutedUserObject = mutedUsersArray.get(i).getAsJsonObject();
                String mutedUser = mutedUserObject.get("username").getAsString();
                long unmuteTime = mutedUserObject.get("unmuteTime").getAsLong();

                if (mutedUser.equalsIgnoreCase(username)) {
                    if (currentTime > unmuteTime) {
                        // Remove the user if the mute duration has expired
                        continue;
                    } else {
                        return true; // User is still muted
                    }
                }
                newMutedUsersArray.add(mutedUserObject);
            }

            // Update the database with the remaining muted users
            jsonObject.add("mutedUsers", newMutedUsersArray);
            writeJsonFile(jsonObject);
        }
        return false; // User is not muted
    }
}
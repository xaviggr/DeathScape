package dc.Persistence.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class BannedWordsDatabase {

    private static String databaseFile;

    public static void setDatabaseFile(String filename) {
        databaseFile = filename;
    }

    public static void initDatabase() {
        File file = new File(databaseFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("bannedWords", new JsonArray());
                    jsonObject.add("mutedUsers", new JsonArray());
                    writeJsonFile(jsonObject);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(databaseFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Métodos para palabras prohibidas
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

    // Métodos para usuarios silenciados
    public static Set<String> getMutedUsers() {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");
        Set<String> mutedUsers = new HashSet<>();

        if (mutedUsersArray != null) {
            mutedUsersArray.forEach(user -> {
                String mutedUser = user.getAsString().trim();
                if (!mutedUser.isEmpty()) {
                    mutedUsers.add(mutedUser);
                }
            });
        }
        return mutedUsers;
    }

    public static void addMutedUser(String username) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray == null) {
            mutedUsersArray = new JsonArray();
            jsonObject.add("mutedUsers", mutedUsersArray);
        }

        mutedUsersArray.add(username);
        writeJsonFile(jsonObject);
    }

    public static void removeMutedUser(String username) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray != null) {
            JsonArray newMutedUsersArray = new JsonArray();
            for (int i = 0; i < mutedUsersArray.size(); i++) {
                String mutedUser = mutedUsersArray.get(i).getAsString();
                if (!mutedUser.equalsIgnoreCase(username)) {
                    newMutedUsersArray.add(mutedUser);
                }
            }
            jsonObject.add("mutedUsers", newMutedUsersArray);
            writeJsonFile(jsonObject);
        }
    }
}

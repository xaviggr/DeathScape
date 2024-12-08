package dc.Persistence.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class BannedWordsDatabase {

    private static String bannedWordsFile;

    public static void setBannedWordsFile(String filename) {
        bannedWordsFile = filename;
    }

    public static void initBannedWordsDatabase() {
        File file = new File(bannedWordsFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"bannedWords\": []}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static JsonObject readJsonFile() {
        File file = new File(bannedWordsFile);
        if (!file.exists()) {
            initBannedWordsDatabase();
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
        try (FileWriter fileWriter = new FileWriter(bannedWordsFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
}

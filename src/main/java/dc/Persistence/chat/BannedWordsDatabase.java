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
                // Crear el archivo si no existe
                file.createNewFile();

                // Crear el objeto JSON inicial
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("bannedWords", new JsonArray());
                jsonObject.add("mutedUsers", new JsonArray());

                // Escribir el objeto JSON al archivo
                writeJsonFile(jsonObject);
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
                JsonObject mutedUserObject = user.getAsJsonObject();
                String mutedUser = mutedUserObject.get("username").getAsString().trim();
                if (!mutedUser.isEmpty()) {
                    mutedUsers.add(mutedUser);
                }
            });
        }
        return mutedUsers;
    }

    public static void addMutedUser(String username, long unmuteTime) {
        JsonObject jsonObject = readJsonFile();
        JsonArray mutedUsersArray = jsonObject.getAsJsonArray("mutedUsers");

        if (mutedUsersArray == null) {
            mutedUsersArray = new JsonArray();
            jsonObject.add("mutedUsers", mutedUsersArray);
        }

        // Crear un objeto JSON para el usuario silenciado
        JsonObject mutedUserObject = new JsonObject();
        mutedUserObject.addProperty("username", username);
        mutedUserObject.addProperty("unmuteTime", unmuteTime);

        mutedUsersArray.add(mutedUserObject);
        writeJsonFile(jsonObject);
    }

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
                    // Verificar si el tiempo de silencio ha pasado
                    if (currentTime > unmuteTime) {
                        // No añadir de nuevo al array (remueve el silencio)
                        continue;
                    } else {
                        return true; // Usuario sigue silenciado
                    }
                }
                newMutedUsersArray.add(mutedUserObject); // Añadir usuarios aún silenciados
            }

            // Actualizar la base de datos con los usuarios que aún están silenciados
            jsonObject.add("mutedUsers", newMutedUsersArray);
            writeJsonFile(jsonObject);
        }
        return false; // Usuario no está silenciado
    }
}

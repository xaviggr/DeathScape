package dc.Persistence.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.UUID;

public class PlayerDatabase {
    public static void setNombreArchivo(String nombreArchivo) {
        PlayerDatabase.nombreArchivo = nombreArchivo;
    }

    private static String nombreArchivo;

    public static void initPlayerDatabase() {
        File archivo = new File (nombreArchivo);

        // Comprueba si el archivo ya existe
        if (!archivo.exists ()) {
            try {
                archivo.createNewFile ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    public static boolean addPlayerDataToDatabase(PlayerData playerData) {
        File archivo = new File(nombreArchivo);

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject;

            if (archivo.exists()) {
                // Si el archivo ya existe, leer su contenido y convertirlo a un objeto JSON
                BufferedReader bufferedReader = new BufferedReader(new FileReader(archivo));
                StringBuilder jsonString = new StringBuilder();
                String linea;

                while ((linea = bufferedReader.readLine()) != null) {
                    jsonString.append(linea);
                }
                bufferedReader.close();

                jsonObject = gson.fromJson(jsonString.toString(), JsonObject.class);
            } else {
                // Si el archivo no existe, crear un nuevo objeto JSON
                jsonObject = new JsonObject();
            }

            if(jsonObject == null) {
                jsonObject = new JsonObject();
            }

            // Buscar si ya existe un objeto con el nombre del jugador
            if (jsonObject.has(playerData.getName())) {
                // Si existe, obtener el objeto y sobrescribir sus valores
                JsonObject existingPlayerObject = jsonObject.getAsJsonObject(playerData.getName());
                existingPlayerObject.addProperty("Name", playerData.getName());
                existingPlayerObject.addProperty("IsDead", playerData.isDead());
                existingPlayerObject.addProperty("Deaths", playerData.getDeaths());
                existingPlayerObject.addProperty("IP", playerData.getHostAddress());
                existingPlayerObject.addProperty("TimePlayed", playerData.getTimePlayed());
                existingPlayerObject.addProperty("UUID", playerData.getUuid().toString());
                existingPlayerObject.addProperty("BanDate", playerData.getBanDate());
                existingPlayerObject.addProperty("BanTime", playerData.getBantime());
                existingPlayerObject.addProperty("Coords", playerData.getCoords());
            } else {
                // Si no existe, crear un nuevo objeto JSON para el jugador
                JsonObject playerObject = new JsonObject();
                playerObject.addProperty("Name", playerData.getName());
                playerObject.addProperty("IsDead", playerData.isDead());
                playerObject.addProperty("Deaths", playerData.getDeaths());
                playerObject.addProperty("IP", playerData.getHostAddress());
                playerObject.addProperty("TimePlayed", playerData.getTimePlayed());
                playerObject.addProperty("UUID", playerData.getUuid().toString());
                playerObject.addProperty("BanDate", playerData.getBanDate());
                playerObject.addProperty("BanTime", playerData.getBantime());
                playerObject.addProperty("Coords", playerData.getCoords());

                jsonObject.add(playerData.getName(), playerObject);
            }

            // Convertir a formato JSON
            String newJsonString = gson.toJson(jsonObject);

            // Escribir en el archivo
            FileWriter fileWriter = new FileWriter(archivo);
            fileWriter.write(newJsonString);
            fileWriter.flush();
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static PlayerData getPlayerDataFromDatabase(String playerName) {
        File archivo = new File(nombreArchivo);

        try {
            Gson gson = new Gson();
            JsonObject jsonObject;

            if (archivo.exists()) {
                // Si el archivo existe, leer su contenido y convertirlo a un objeto JSON
                BufferedReader bufferedReader = new BufferedReader(new FileReader(archivo));
                StringBuilder jsonString = new StringBuilder();
                String linea;

                while ((linea = bufferedReader.readLine()) != null) {
                    jsonString.append(linea);
                }
                bufferedReader.close();

                jsonObject = gson.fromJson(jsonString.toString(), JsonObject.class);

                if (jsonObject == null) {
                    jsonObject = new JsonObject();
                }

                // Buscar el objeto del jugador por su nombre
                JsonObject playerObject = jsonObject.getAsJsonObject(playerName);

                if (playerObject != null) {
                    // Obtener los datos del jugador desde el objeto JSON
                    String name = playerName;
                    boolean isDead = playerObject.get("IsDead").getAsBoolean();
                    int deaths = playerObject.get("Deaths").getAsInt();
                    String hostAddress = playerObject.get("IP").getAsString();
                    String timePlayed = playerObject.get("TimePlayed").getAsString();
                    UUID uuid = UUID.fromString(playerObject.get("UUID").getAsString());
                    String banDate = playerObject.get("BanDate").getAsString();
                    String banTime = playerObject.get("BanTime").getAsString();
                    String coords = playerObject.get("Coords").getAsString();

                    // Crear y devolver un nuevo objeto PlayerData
                    return new PlayerData(name, isDead, deaths, hostAddress, timePlayed, uuid, banDate, banTime, coords);
                }
            } else {
                // Si el archivo no existe, devolver null
                initPlayerDatabase ();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Si no se encuentra el jugador, devolver null
    }
}

package dc.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dc.DeathScape;

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

            // Añadir los nuevos datos al objeto JSON
            JsonObject playerObject = new JsonObject();
            playerObject.addProperty("Name", playerData.name);
            playerObject.addProperty("IsDead", playerData.isDead);
            playerObject.addProperty("Deaths", playerData.deaths);
            playerObject.addProperty("IP", playerData.hostAddress);
            playerObject.addProperty("TimePlayed", playerData.timePlayed);
            playerObject.addProperty("UUID", playerData.uuid.toString());

            jsonObject.add(playerData.name, playerObject);

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
        try {
            File archivo = new File(nombreArchivo);
            if (archivo.exists()) {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(archivo));
                StringBuilder stringBuilder = new StringBuilder();
                String linea;

                // Leer el contenido del archivo
                while ((linea = bufferedReader.readLine()) != null) {
                    stringBuilder.append(linea);
                }
                bufferedReader.close();

                // Convertir a objeto JSON
                String contenido = stringBuilder.toString();
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(contenido).getAsJsonObject();

                // Buscar el objeto por el nombre del jugador
                if (jsonObject.has(playerName)) {
                    JsonObject playerObject = jsonObject.get(playerName).getAsJsonObject();

                    // Extraer datos del objeto JSON y crear un objeto PlayerData
                    String name = playerObject.get("Name").getAsString();
                    boolean isDead = playerObject.get("IsDead").getAsBoolean();
                    int deaths = playerObject.get("Deaths").getAsInt();
                    String hostAddress = playerObject.get("IP").getAsString();
                    String timePlayed = playerObject.get("TimePlayed").getAsString();
                    UUID uuid = UUID.fromString(playerObject.get("UUID").getAsString());

                    return new PlayerData(name, isDead, deaths, hostAddress, timePlayed, uuid);
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

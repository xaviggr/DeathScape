package dc.Persistence.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportsDatabase {

    private static String reportsFile;

    public static void setReportsFile(String filename) {
        reportsFile = filename;
    }

    public static void initReportsDatabase() {
        File file = new File(reportsFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"reports\": []}");  // Inicia con un array vacío de reportes
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static JsonObject readJsonFile() {
        File file = new File(reportsFile);
        if (!file.exists()) {
            initReportsDatabase();
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
        try (FileWriter fileWriter = new FileWriter(reportsFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addReport(String reporter, String reported, String reason) {
        JsonObject jsonObject = readJsonFile();
        JsonArray reportsArray = jsonObject.getAsJsonArray("reports");

        if (reportsArray == null) {
            reportsArray = new JsonArray();
            jsonObject.add("reports", reportsArray);
        }

        // Obtener la fecha actual en formato ISO 8601
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        // Crear un nuevo reporte
        JsonObject newReport = new JsonObject();
        newReport.addProperty("reporter", reporter);
        newReport.addProperty("reported", reported);
        newReport.addProperty("reason", reason);
        newReport.addProperty("timestamp", timestamp);

        // Agregar el reporte al array
        reportsArray.add(newReport);
        writeJsonFile(jsonObject);
    }

    public static JsonArray getReports() {
        JsonObject jsonObject = readJsonFile();
        return jsonObject.getAsJsonArray("reports");
    }

    public static void removeReport(int index) {
        JsonObject jsonObject = readJsonFile();
        JsonArray reportsArray = jsonObject.getAsJsonArray("reports");

        if (reportsArray != null && index >= 0 && index < reportsArray.size()) {
            reportsArray.remove(index);
            jsonObject.add("reports", reportsArray);
            writeJsonFile(jsonObject);
        }
    }

    public static void clearReports() {
        JsonObject jsonObject = readJsonFile();
        jsonObject.add("reports", new JsonArray());  // Limpia los reportes
        writeJsonFile(jsonObject);
    }
}

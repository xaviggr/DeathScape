package dc.Persistence.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Manages the reporting system for the server, including adding, retrieving,
 * removing, and clearing player reports. All data is stored in a JSON-based
 * database file.
 */
public class ReportsDatabase {

    private static String reportsFile; // Path to the JSON reports file

    /**
     * Sets the path for the reports database file.
     *
     * @param filename The path to the reports file.
     */
    public static void setReportsFile(String filename) {
        reportsFile = filename;
    }

    /**
     * Initializes the reports database file. If the file does not exist, it creates a new one
     * with a default JSON structure for storing reports.
     */
    public static void initReportsDatabase() {
        File file = new File(reportsFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"reports\": []}"); // Initialize with an empty reports array
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the JSON reports database file and returns its contents as a `JsonObject`.
     *
     * @return The `JsonObject` representation of the database file.
     */
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

    /**
     * Writes a `JsonObject` to the reports database file.
     *
     * @param jsonObject The `JsonObject` to write to the file.
     */
    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(reportsFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new report to the database. Each report includes the reporter, reported player,
     * reason, and a timestamp in ISO 8601 format.
     *
     * @param reporter The name of the player submitting the report.
     * @param reported The name of the player being reported.
     * @param reason   The reason for the report.
     */
    public static void addReport(String reporter, String reported, String reason) {
        JsonObject jsonObject = readJsonFile();
        JsonArray reportsArray = jsonObject.getAsJsonArray("reports");

        if (reportsArray == null) {
            reportsArray = new JsonArray();
            jsonObject.add("reports", reportsArray);
        }

        // Get the current date and time in ISO 8601 format
        String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        // Create a new report
        JsonObject newReport = new JsonObject();
        newReport.addProperty("reporter", reporter);
        newReport.addProperty("reported", reported);
        newReport.addProperty("reason", reason);
        newReport.addProperty("timestamp", timestamp);

        // Add the report to the array
        reportsArray.add(newReport);
        writeJsonFile(jsonObject);
    }

    /**
     * Retrieves all reports from the database as a `JsonArray`.
     *
     * @return A `JsonArray` of all reports.
     */
    public static JsonArray getReports() {
        JsonObject jsonObject = readJsonFile();
        return jsonObject.getAsJsonArray("reports");
    }

    /**
     * Removes a report from the database by its index in the array.
     *
     * @param index The index of the report to remove.
     */
    public static void removeReport(int index) {
        JsonObject jsonObject = readJsonFile();
        JsonArray reportsArray = jsonObject.getAsJsonArray("reports");

        if (reportsArray != null && index >= 0 && index < reportsArray.size()) {
            reportsArray.remove(index);
            jsonObject.add("reports", reportsArray);
            writeJsonFile(jsonObject);
        }
    }

    /**
     * Clears all reports from the database.
     */
    public static void clearReports() {
        JsonObject jsonObject = readJsonFile();
        jsonObject.add("reports", new JsonArray()); // Reset the reports array
        writeJsonFile(jsonObject);
    }
}
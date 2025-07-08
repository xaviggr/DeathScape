package dc.Persistence.logs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import dc.Business.log.LogData;

/**
 * A database class for managing logs.
 * Provides methods to load, write, add, and remove logs stored in a JSON file.
 */
public class LogDatabase {

    private static String logFile; // Path to the file storing logs

    /**
     * Sets the file path for the log database.
     *
     * @param filename The file path to be used for storing logs.
     */
    public static void setLogFile(String filename) {
        logFile = filename;
    }

    /**
     * Initializes the log database by creating the file if it doesn't exist.
     * The database is initialized with an empty JSON array of logs.
     */
    public static void initLogDatabase() {
        File file = new File(logFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"logs\": []}"); // Initialize with an empty array of logs
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads the JSON file containing log data.
     *
     * @return A JsonObject containing the log data.
     */
    private static JsonObject readJsonFile() {
        File file = new File(logFile);
        if (!file.exists()) {
            initLogDatabase();
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
     * Writes the given log data to the JSON file.
     *
     * @param jsonObject A JsonObject containing the updated log data.
     */
    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(logFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all logs from the database.
     *
     * @return A list of {@link LogData} objects representing the logs.
     */
    public static List<LogData> getLogs() {
        JsonObject jsonObject = readJsonFile();
        JsonArray logsArray = jsonObject.getAsJsonArray("logs");
        List<LogData> logs = new ArrayList<>();

        if (logsArray != null) {
            Gson gson = new Gson();
            logsArray.forEach(log -> logs.add(gson.fromJson(log, LogData.class)));
        }
        return logs;
    }

    /**
     * Adds a new log to the database.
     *
     * @param logData The {@link LogData} object representing the log to be added.
     */
    public static void addLog(LogData logData) {
        JsonObject jsonObject = readJsonFile();
        JsonArray logsArray = jsonObject.getAsJsonArray("logs");

        if (logsArray == null) {
            logsArray = new JsonArray();
            jsonObject.add("logs", logsArray);
        }

        logsArray.add(new Gson().toJsonTree(logData));
        writeJsonFile(jsonObject);
    }

    /**
     * Removes a specific log from the database.
     *
     * @param logData The {@link LogData} object representing the log to be removed.
     */
    public static void removeLog(LogData logData) {
        JsonObject jsonObject = readJsonFile();
        JsonArray logsArray = jsonObject.getAsJsonArray("logs");

        if (logsArray != null) {
            JsonArray newLogsArray = new JsonArray();
            Gson gson = new Gson();
            for (int i = 0; i < logsArray.size(); i++) {
                LogData existingLog = gson.fromJson(logsArray.get(i), LogData.class);
                if (!existingLog.toString().equals(logData.toString())) {
                    newLogsArray.add(logsArray.get(i));
                }
            }
            jsonObject.add("logs", newLogsArray);
            writeJsonFile(jsonObject);
        }
    }
}
package dc.Persistence.logs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import dc.Business.log.LogData;

public class LogDatabase {

    private static String logFile;

    public static void setLogFile(String filename) {
        logFile = filename;
    }

    public static void initLogDatabase() {
        File file = new File(logFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"logs\": []}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private static void writeJsonFile(JsonObject jsonObject) {
        try (FileWriter fileWriter = new FileWriter(logFile)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            fileWriter.write(gson.toJson(jsonObject));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

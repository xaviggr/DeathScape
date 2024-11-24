package dc.Business.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogData {

    private String logType; // Tipo de log
    private String author;  // Autor del log
    private String logInfo; // Información del log
    private String dateTime; // Fecha y hora del log en formato legible

    // Constructor
    public LogData(String logType, String author, String logInfo) {
        this.logType = logType;
        this.author = author;
        this.logInfo = logInfo;

        // Establece la fecha y hora actual en formato legible
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dateTime = LocalDateTime.now().format(formatter);
    }

    // Getters y Setters
    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLogInfo() {
        return logInfo;
    }

    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    // Convierte el objeto a un String en formato JSON
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}

package dc.Business.log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a log entry containing information about the log type, author,
 * log details, and the timestamp of when the log was created.
 */
@SuppressWarnings("unused")
public class LogData {

    private String logType; // The type of log (e.g., INFO, ERROR)
    private String author;  // The author or source of the log
    private String logInfo; // The detailed information of the log
    private String dateTime; // The timestamp of the log in a readable format

    /**
     * Constructs a `LogData` object with the specified type, author, and information.
     * Automatically sets the current date and time as the timestamp.
     *
     * @param logType The type of the log (e.g., INFO, ERROR, DEBUG).
     * @param author  The author or source of the log.
     * @param logInfo Detailed information about the log.
     */
    public LogData(String logType, String author, String logInfo) {
        this.logType = logType;
        this.author = author;
        this.logInfo = logInfo;

        // Sets the current date and time in a readable format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.dateTime = LocalDateTime.now().format(formatter);
    }

    /**
     * Gets the type of the log.
     *
     * @return The log type.
     */
    public String getLogType() {
        return logType;
    }

    /**
     * Sets the type of the log.
     *
     * @param logType The log type to set.
     */
    public void setLogType(String logType) {
        this.logType = logType;
    }

    /**
     * Gets the author of the log.
     *
     * @return The log's author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of the log.
     *
     * @param author The author to set.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the detailed information of the log.
     *
     * @return The log information.
     */
    public String getLogInfo() {
        return logInfo;
    }

    /**
     * Sets the detailed information of the log.
     *
     * @param logInfo The log information to set.
     */
    public void setLogInfo(String logInfo) {
        this.logInfo = logInfo;
    }

    /**
     * Gets the timestamp of the log.
     *
     * @return The log's timestamp.
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Sets the timestamp of the log.
     *
     * @param dateTime The timestamp to set.
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Converts the `LogData` object to a JSON string.
     *
     * @return The JSON representation of the `LogData` object.
     */
    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}

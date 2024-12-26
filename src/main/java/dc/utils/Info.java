package dc.utils;

import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class to provide general information about the server and current date.
 */
public class Info {

    /**
     * Counts the number of online players with administrative permissions.
     *
     * @return The count of players who are currently online and have the "admin" permission.
     *         Replace "admin" with the actual permission node used for administrators in your server setup.
     */
    public static int getAdminCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("admin")) // Replace "admin" with the correct permission node.
                .count();
    }

    /**
     * Retrieves the current date in the format "dd.MM.yyyy".
     *
     * @return A string representation of the current date, formatted as "day.month.year".
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new Date());
    }
}
package dc.utils;

import org.bukkit.Bukkit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Info {

    public static int getAdminCount() {
        return (int) Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("admin")) // Cambia "admin" al permiso real que usas para los administradores
                .count();
    }

    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(new Date());
    }
}

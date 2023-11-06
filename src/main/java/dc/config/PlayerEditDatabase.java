package dc.config;

import org.bukkit.entity.Player;

public class PlayerEditDatabase {

    public static void setPlayerAsDeath(Player playerDeath) {

        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (playerDeath.getName ());
        if (playerdata == null) {
            playerDeath.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setDeaths(playerdata.getDeaths() + 1);
            playerdata.setDead(true);
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerBanDate(Player player) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setBanTime();
            playerdata.setBanDate();
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerTimePlayed(Player player, int new_segundos, int new_minutos, int new_horas) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {

            String[] partes = playerdata.getTimePlayed().split("\\s+");

            int segundos = 0, minutos = 0, horas = 0;

            for (String parte : partes) {
                if (parte.endsWith("s")) {
                    segundos = Integer.parseInt(parte.substring(0, parte.length() - 1)); // Convertir los segundos a entero
                } else if (parte.endsWith("m")) {
                    minutos = Integer.parseInt(parte.substring(0, parte.length() - 1));// Convertir los minutos a entero
                } else if (parte.endsWith("h")) {
                    horas = Integer.parseInt(parte.substring(0, parte.length() - 1));// Convertir las horas a entero
                }
            }

            segundos += new_segundos;
            if (segundos >= 60) {
                new_minutos += segundos / 60;
                segundos = segundos % 60;
            }
            minutos += new_minutos;
            if (minutos >= 60) {
                new_horas += minutos / 60;
                minutos = minutos % 60;
            }
            horas += new_horas;

            playerdata.setTimePlayed(segundos + "s " + minutos + "m " + horas + "h");
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void setPlayerCoords(Player player) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player.getName ());
        if (playerdata == null) {
            player.kickPlayer ("Error al cargar tus datos, contacta con un administrador.");
        } else {
            playerdata.setCoords(player.getLocation().getX() + "," + player.getLocation().getY() + "," + player.getLocation().getZ());
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }

    public static void UnbanPlayer(String player_name) {
        PlayerData playerdata = PlayerDatabase.getPlayerDataFromDatabase (player_name);
        if (playerdata != null) {
            playerdata.setDead(false);
            playerdata.setBanDate("0");
            playerdata.setBantime("0");
            PlayerDatabase.addPlayerDataToDatabase (playerdata);
        }
    }
}

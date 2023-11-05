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
            playerdata.setDeathTime();
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
                int parseInt = Integer.parseInt(parte.substring(0, parte.length() - 1));
                if (parte.endsWith("s")) {
                    segundos = parseInt; // Convertir los segundos a entero
                } else if (parte.endsWith("m")) {
                    minutos = parseInt; // Convertir los minutos a entero
                } else if (parte.endsWith("h")) {
                    horas = parseInt; // Convertir las horas a entero
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
}

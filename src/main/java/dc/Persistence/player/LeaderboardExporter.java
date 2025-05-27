package dc.Persistence.player;

import dc.Business.player.PlayerData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LeaderboardExporter {

    public static void exportLeaderboardAsHtml(String outputPath) {
        List<PlayerData> leaderboard = PlayerDatabase.getLeaderboard();

        File file = new File(outputPath);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("<!DOCTYPE html>\n<html>\n<head>\n<title>Leaderboard</title>\n");
            writer.write("<style>\n");
            writer.write("table { width: 100%%; border-collapse: collapse; }\n");
            writer.write("th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }\n");
            writer.write("</style>\n</head>\n<body>\n");

            writer.write("<h2>Leaderboard de Puntos</h2>\n");
            writer.write("<table>\n<tr><th>#</th><th>Jugador</th><th>Puntos</th></tr>\n");

            int rank = 1;
            for (PlayerData player : leaderboard) {
                String playerName = player.getName();
                int points = player.getPoints();
                String avatarUrl = "https://crafatar.com/avatars/" + playerName;

                writer.write(String.format(
                        "<tr><td>%d</td><td><img src='%s' width='32' height='32' style='vertical-align:middle; border-radius:4px;'> %s</td><td>%d</td></tr>\n",
                        rank++, avatarUrl, playerName, points
                ));
            }

            writer.write("</table>\n</body>\n</html>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

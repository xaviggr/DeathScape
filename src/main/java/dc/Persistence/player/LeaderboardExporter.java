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
            writer.write("<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n");
            writer.write("  <meta charset=\"UTF-8\">\n  <title>Leaderboard</title>\n  <style>\n");
            writer.write("    body { font-family: Arial, sans-serif; background-color: #1a1a1a; color: #fff; padding: 20px; }\n");
            writer.write("    h2 { text-align: center; color: #f0f0f0; }\n");
            writer.write("    table { width: 100%; border-collapse: collapse; margin-top: 20px; background-color: #2b2b2b; border-radius: 10px; overflow: hidden; }\n");
            writer.write("    th, td { padding: 12px 16px; text-align: left; }\n");
            writer.write("    th { background-color: #3a3a3a; color: #ccc; }\n");
            writer.write("    tr:nth-child(even) { background-color: #242424; }\n");
            writer.write("    tr:hover { background-color: #595236; }\n");
            writer.write("    .player-info { display: flex; align-items: center; gap: 10px; }\n");
            writer.write("    .avatar { border-radius: 5px; }\n");
            writer.write("    .rank { font-weight: bold; color: gold; }\n");
            writer.write("    #pagination-controls { text-align: center; margin-top: 20px; }\n");
            writer.write("    button { padding: 10px 20px; font-size: 16px; background-color: #3a3a3a; color: white; border: none; border-radius: 5px; cursor: pointer; }\n");
            writer.write("    button:hover { background-color: #595236; }\n");
            writer.write("  </style>\n</head>\n<body>\n");

            writer.write("  <table id='leaderboardTable'>\n<thead>\n<tr><th>#</th><th>Jugador</th><th>Puntos</th></tr>\n</thead>\n<tbody>\n");

            int rank = 1;
            for (PlayerData player : leaderboard) {
                String name = player.getName();
                int points = player.getPoints();
                String avatarUrl = "https://crafatar.com/avatars/" + player.getUuid();

                writer.write(String.format(
                        "  <tr class='row'>\n" +
                                "    <td class='rank'>%d</td>\n" +
                                "    <td class='player-info'><img class='avatar' src='%s' width='32' height='32'> %s</td>\n" +
                                "    <td>%d</td>\n" +
                                "  </tr>\n", rank++, avatarUrl, name, points
                ));
            }

            writer.write("  </tbody>\n</table>\n");

            writer.write("  <div id='pagination-controls'>\n" +
                    "    <button onclick='previousPage()'>Anterior</button>\n" +
                    "    <button onclick='nextPage()'>Siguiente</button>\n" +
                    "  </div>\n");

            writer.write("  <script>\n" +
                    "    let currentPage = 0;\n" +
                    "    const rowsPerPage = 100;\n" +
                    "    const rows = document.querySelectorAll('.row');\n" +
                    "    function showPage(page) {\n" +
                    "      const start = page * rowsPerPage;\n" +
                    "      const end = start + rowsPerPage;\n" +
                    "      rows.forEach((row, index) => {\n" +
                    "        row.style.display = (index >= start && index < end) ? '' : 'none';\n" +
                    "      });\n" +
                    "    }\n" +
                    "    function nextPage() {\n" +
                    "      if ((currentPage + 1) * rowsPerPage < rows.length) {\n" +
                    "        currentPage++;\n" +
                    "        showPage(currentPage);\n" +
                    "      }\n" +
                    "    }\n" +
                    "    function previousPage() {\n" +
                    "      if (currentPage > 0) {\n" +
                    "        currentPage--;\n" +
                    "        showPage(currentPage);\n" +
                    "      }\n" +
                    "    }\n" +
                    "    showPage(0);\n" +
                    "  </script>\n");

            writer.write("</body>\n</html>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

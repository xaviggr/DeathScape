package dc.Persistence.leaderboard;

import dc.Business.player.PlayerData;
import dc.Persistence.player.PlayerDatabase;

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
            writer.write("    body { font-family: Arial, sans-serif; background-color: #1a1a1a; color: #fff; padding: 20px; margin: 0; }\n");
            writer.write("    h2 { text-align: center; color: #f0f0f0; }\n");
            writer.write("    table { width: 100%; border-collapse: collapse; margin-top: 20px; background-color: #2b2b2b; border-radius: 10px; overflow: hidden; }\n");
            writer.write("    th, td { padding: 12px 16px; text-align: left; }\n");
            writer.write("    th { background-color: #3a3a3a; color: #ccc; }\n");
            writer.write("    tr:nth-child(even) { background-color: #242424; }\n");
            writer.write("    tr:hover { background-color: #595236; }\n");
            writer.write("    .player-info { display: flex; align-items: center; gap: 10px; }\n");
            writer.write("    .avatar { border-radius: 5px; }\n");
            writer.write("    .rank { font-weight: bold; color: gold; }\n");

            // Pagination style
            writer.write("    .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 10px; }\n");
            writer.write("    .pagination-controls button { background: none; color: #fff; border: none; font-weight: bold; margin: 0 4px; cursor: pointer; font-size: 14px; position: relative; }\n");
            writer.write("    .pagination-controls button:hover { color: #bfa76f; }\n");
            writer.write("    .pagination-controls input { background-color: #2b2b2b; color: white; width: 40px; text-align: center; border: none; border-bottom: 2px solid yellow; font-weight: bold; border-radius: 4px; }\n");
            writer.write("    .pagination-controls input:focus { outline: none; border-bottom: 2px solid #bfa76f; }\n");

            writer.write("  </style>\n</head>\n<body>\n");

            // Pagination TOP
            writer.write("  <div class='pagination-wrapper'>\n" +
                    "    <div class='pagination-controls'>\n" +
                    "      <button onclick='previousPage()'>Previous</button>\n" +
                    "      <input type='number' id='pageInput' min='1' value='1' onchange='goToInputPage()'>\n" +
                    "      <button onclick='nextPage()'>Next</button>\n" +
                    "    </div>\n" +
                    "  </div>\n");

            // Table
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

            // Pagination BOTTOM
            writer.write("  <div class='pagination-wrapper'>\n" +
                    "    <div class='pagination-controls'>\n" +
                    "      <button onclick='previousPage()'>Previous</button>\n" +
                    "      <input type='number' id='pageInputBottom' min='1' value='1' onchange='goToInputPageBottom()'>\n" +
                    "      <button onclick='nextPage()'>Next</button>\n" +
                    "    </div>\n" +
                    "  </div>\n");

            // Script
            writer.write("  <script>\n" +
                    "    let currentPage = 0;\n" +
                    "    const rowsPerPage = 50;\n" +
                    "    const rows = document.querySelectorAll('.row');\n" +
                    "    function showPage(page) {\n" +
                    "      const totalPages = Math.ceil(rows.length / rowsPerPage);\n" +
                    "      if (page < 0 || page >= totalPages) return;\n" +
                    "      const start = page * rowsPerPage;\n" +
                    "      const end = start + rowsPerPage;\n" +
                    "      rows.forEach((row, index) => {\n" +
                    "        row.style.display = (index >= start && index < end) ? '' : 'none';\n" +
                    "      });\n" +
                    "      currentPage = page;\n" +
                    "      document.getElementById('pageInput').value = page + 1;\n" +
                    "      document.getElementById('pageInputBottom').value = page + 1;\n" +
                    "      sendHeight();\n" +
                    "    }\n" +
                    "    function nextPage() {\n" +
                    "      showPage(currentPage + 1);\n" +
                    "    }\n" +
                    "    function previousPage() {\n" +
                    "      showPage(currentPage - 1);\n" +
                    "    }\n" +
                    "    function goToInputPage() {\n" +
                    "      const input = parseInt(document.getElementById('pageInput').value) - 1;\n" +
                    "      showPage(input);\n" +
                    "    }\n" +
                    "    function goToInputPageBottom() {\n" +
                    "      const input = parseInt(document.getElementById('pageInputBottom').value) - 1;\n" +
                    "      showPage(input);\n" +
                    "    }\n" +
                    "    function sendHeight() {\n" +
                    "      const height = document.body.scrollHeight;\n" +
                    "      window.parent.postMessage({ height }, '*');\n" +
                    "    }\n" +
                    "    window.addEventListener('load', () => { showPage(0); sendHeight(); });\n" +
                    "    window.addEventListener('resize', sendHeight);\n" +
                    "  </script>\n");

            writer.write("</body>\n</html>\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

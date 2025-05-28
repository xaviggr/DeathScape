package dc.Persistence.leaderboard;

import dc.Persistence.config.MainConfigManager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubUploader {

    private static final String USER = "konozca-david-vilar";
    private static final String REPO = "leaderboard";
    private static final String FILE_PATH = "index.html";
    private static final String TOKEN = MainConfigManager.getInstance().getGithubToken();

    public static void uploadFile(String localFilePath) throws IOException {
        File file = new File(localFilePath);
        if (!file.exists()) return;

        String content = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
        String apiUrl = "https://api.github.com/repos/" + USER + "/" + REPO + "/contents/" + FILE_PATH;

        // Paso 1: obtener el SHA actual
        String sha = null;
        try {
            HttpURLConnection getConn = (HttpURLConnection) new URL(apiUrl).openConnection();
            getConn.setRequestMethod("GET");
            getConn.setRequestProperty("Authorization", "token " + TOKEN);
            getConn.setRequestProperty("Accept", "application/vnd.github+json");

            if (getConn.getResponseCode() == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(getConn.getInputStream()))) {
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) json.append(line);

                    Pattern pattern = Pattern.compile("\"sha\":\\s*\"([^\"]+)\"");
                    Matcher matcher = pattern.matcher(json.toString());
                    if (matcher.find()) sha = matcher.group(1);
                }
            }
        } catch (Exception e) {
            System.out.println("Archivo no existe aún. Se subirá por primera vez.");
        }

        // Paso 2: hacer PUT con o sin SHA
        StringBuilder payload = new StringBuilder();
        payload.append("{\n");
        payload.append("\"message\": \"Actualización automática del leaderboard\",\n");
        payload.append("\"committer\": { \"name\": \"AutoUploader\", \"email\": \"auto@plugin.com\" },\n");
        payload.append("\"content\": \"").append(content).append("\"");

        if (sha != null) {
            payload.append(",\n\"sha\": \"").append(sha).append("\"");
        }

        payload.append("\n}");

        HttpURLConnection putConn = (HttpURLConnection) new URL(apiUrl).openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Authorization", "token " + TOKEN);
        putConn.setRequestProperty("Content-Type", "application/json");
        putConn.setDoOutput(true);

        try (OutputStream os = putConn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = putConn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
        } else {
            System.err.println("Error subiendo a GitHub: " + responseCode);
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(putConn.getErrorStream()))) {
                errorReader.lines().forEach(System.err::println);
            }
        }
    }
}

package dc.Persistence.leaderboard;

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

    // 🔐 1. Leer el token desde el archivo seguro
    private static String getToken() throws IOException {
        File file = new File("plugins/DeathScape/.github_token");
        if (!file.exists()) {
            throw new FileNotFoundException("Token file not found: " + file.getAbsolutePath());
        }
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
    }

    public static void uploadFile(String localFilePath) throws IOException {
        String token = getToken();

        File localFile = new File(localFilePath);
        if (!localFile.exists()) {
            throw new FileNotFoundException("Locals file not found: " + localFilePath);
        }

        // 📦 Codifica el contenido del archivo en base64
        String content = Base64.getEncoder().encodeToString(Files.readAllBytes(localFile.toPath()));
        String apiUrl = "https://api.github.com/repos/" + USER + "/" + REPO + "/contents/" + FILE_PATH;

        // 📥 Obtener el SHA actual si el archivo ya existe
        String sha = null;
        try {
            HttpURLConnection getConn = (HttpURLConnection) new URL(apiUrl).openConnection();
            getConn.setRequestMethod("GET");
            getConn.setRequestProperty("Authorization", "token " + token);
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

        // 📤 Preparar el JSON con el contenido
        StringBuilder payload = new StringBuilder();
        payload.append("{\n");
        payload.append("\"message\": \"Actualización automática del leaderboard\",\n");
        payload.append("\"committer\": { \"name\": \"AutoUploader\", \"email\": \"auto@plugin.com\" },\n");
        payload.append("\"content\": \"").append(content).append("\"");
        if (sha != null) {
            payload.append(",\n\"sha\": \"").append(sha).append("\"");
        }
        payload.append("\n}");

        // 📡 Hacer la solicitud PUT
        HttpURLConnection putConn = (HttpURLConnection) new URL(apiUrl).openConnection();
        putConn.setRequestMethod("PUT");
        putConn.setRequestProperty("Authorization", "token " + token);
        putConn.setRequestProperty("Content-Type", "application/json");
        putConn.setDoOutput(true);

        try (OutputStream os = putConn.getOutputStream()) {
            os.write(payload.toString().getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = putConn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("Leaderboard actualizado en GitHub.");
        } else {
            System.err.println("❌ Error subiendo a GitHub: " + responseCode);
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(putConn.getErrorStream()))) {
                errorReader.lines().forEach(System.err::println);
            }
        }
    }
}

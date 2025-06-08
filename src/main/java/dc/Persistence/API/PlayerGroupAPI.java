package dc.Persistence.API;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlayerGroupAPI {

    private static final String FIREBASE_URL = "https://deathscape-3dc56-default-rtdb.europe-west1.firebasedatabase.app/playerGroups/";

    public static void setPlayerGroup(String username, String group) {
        try {
            String urlStr = FIREBASE_URL + username + ".json";
            URL url = new URL(urlStr);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("PUT");
            httpConn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            httpConn.setDoOutput(true);

            // Payload en JSON: "tier1", "tier2", etc.
            String jsonPayload = "\"" + group + "\"";

            try (OutputStream os = httpConn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = httpConn.getResponseCode();
            System.out.println("[FirebaseAPI] Respuesta HTTP: " + responseCode + " al actualizar grupo de " + username);

            httpConn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

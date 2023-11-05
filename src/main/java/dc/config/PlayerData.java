package dc.config;

import javax.naming.Name;
import java.util.UUID;

public class PlayerData {

    public String name;
    public boolean isDead;
    public int deaths;
    public String hostAddress;
    public String timePlayed;
    public UUID uuid;
    public PlayerData(String name, boolean isDead, int deaths, String hostAddress, String timePlayed, UUID uniqueId) {
        this.name = name;
        this.isDead = isDead;
        this.deaths = deaths;
        this.hostAddress = hostAddress;
        this.timePlayed = timePlayed;
        this.uuid = uniqueId;
    }
}

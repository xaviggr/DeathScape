package dc.Business.player;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlayerData {

    private final String name;
    private boolean isDead;
    private int deaths;
    private final String hostAddress;
    private String timePlayed;
    private final UUID uuid;
    private int points;

    private String banDate;
    private String bantime;

    private String coords;
    private String dimension;
    private String group;
    private double health;

    public String getBanDate() {
        return banDate;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public String getName() {
        return name;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean dead) {
        isDead = dead;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public String getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(String timePlayed) {
        this.timePlayed = timePlayed;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public void setBanDate(String banDate) {
        this.banDate = banDate;
    }

    public String getBantime() {
        return bantime;
    }

    public void setBantime(String bantime) {
        this.bantime = bantime;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public PlayerData(String name, boolean isDead, int deaths, String hostAddress, String timePlayed, UUID uniqueId, String banDate, String bantime, String coords, int points, String group) {
        this.name = name;
        this.isDead = isDead;
        this.deaths = deaths;
        this.hostAddress = hostAddress;
        this.timePlayed = timePlayed;
        this.uuid = uniqueId;
        this.banDate = banDate;
        this.bantime= bantime;
        this.coords = coords;
        this.points = points;
        this.group = group;
        this.dimension = "world";
        this.health = 20;
    }

    public void setBanTime() {

        LocalDateTime fechaActual = LocalDateTime.now();

        int sec = fechaActual.getSecond();
        int min = fechaActual.getMinute();
        int hour = fechaActual.getHour();

        String fSec;
        String fMin;
        String fHour;

        if (sec < 10) {

            fSec = "0" + sec;
        } else {

            fSec = String.valueOf(sec);
        }

        if (min < 10) {

            fMin = "0" + min;
        } else {

            fMin = String.valueOf(min);
        }

        if (hour < 10) {

            fHour = "0" + hour;
        } else {

            fHour = String.valueOf(hour);
        }

        bantime = fHour + ":" + fMin + ":" + fSec;
    }

    public void setBanDate() {

            LocalDateTime fechaActual = LocalDateTime.now();

            int day = fechaActual.getDayOfMonth();
            int month = fechaActual.getMonthValue();
            int year = fechaActual.getYear();

            String fDay;
            String fMonth;
            String fYear;

            if (day < 10) {

                fDay = "0" + day;
            } else {

                fDay = String.valueOf(day);
            }

            if (month < 10) {

                fMonth = "0" + month;
            } else {

                fMonth = String.valueOf(month);
            }

            if (year < 10) {

                fYear = "0" + year;
            } else {

                fYear = String.valueOf(year);
            }

            banDate = fDay + "/" + fMonth + "/" + fYear;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public World getDimension() {
        return Bukkit.getWorld(dimension);
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }
}

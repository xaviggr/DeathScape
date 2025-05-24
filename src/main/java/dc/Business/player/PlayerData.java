package dc.Business.player;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the data associated with a player, including information about
 * their deaths, bans, coordinates, health, group, and more.
 */
public class PlayerData {

    private final String name; // The player's name
    private boolean isDead; // Whether the player is dead
    private int deaths; // Number of times the player has died
    private final String hostAddress; // The player's IP address
    private String timePlayed; // Total time the player has spent in the game
    private final UUID uuid; // The player's unique ID
    private int points; // The player's points in the game

    private String banDate; // Date of the player's ban
    private String bantime; // Time of the player's ban

    private String coords; // The player's coordinates
    private String dimension; // The player's current dimension
    private String group; // The player's group
    private double health; // The player's health
    private int lifes;

    /**
     * Constructs a `PlayerData` object with the specified details.
     *
     * @param name        The player's name.
     * @param isDead      Whether the player is dead.
     * @param deaths      Number of deaths for the player.
     * @param hostAddress The player's IP address.
     * @param timePlayed  Total time the player has spent in the game.
     * @param uniqueId    The player's unique ID.
     * @param banDate     The date of the player's ban.
     * @param bantime     The time of the player's ban.
     * @param coords      The player's coordinates.
     * @param points      The player's points.
     * @param group       The player's group.
     * @param lifes       The player's lifes.
     */
    public PlayerData(String name, boolean isDead, int deaths, String hostAddress, String timePlayed, UUID uniqueId, String banDate, String bantime, String coords, int points, String group, int lifes) {
        this.name = name;
        this.isDead = isDead;
        this.deaths = deaths;
        this.hostAddress = hostAddress;
        this.timePlayed = timePlayed;
        this.uuid = uniqueId;
        this.banDate = banDate;
        this.bantime = bantime;
        this.coords = coords;
        this.points = points;
        this.group = group;
        this.dimension = "world"; // Default dimension
        this.health = 20; // Default health
        this.lifes = lifes; // Default lifes
    }

    // ------------------- Getters and Setters -------------------

    public String getBanDate() {
        return banDate;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getCoords() {
        return coords;
    }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public double getHealth() {
        return health;
    }

    public void setHealth(double health) {
        this.health = health;
    }

    public World getDimension() {
        return Bukkit.getWorld(dimension);
    }

    public void setDimension(String dimension) {
        this.dimension = dimension;
    }

    public int getLifes() { return lifes; }

    public void setLifes(int lifes) { this.lifes = lifes; }

    // ------------------- Custom Methods -------------------

    /**
     * Sets the current date as the ban date.
     */
    public void setBanDate() {
        LocalDateTime currentDate = LocalDateTime.now();
        banDate = String.format("%02d/%02d/%d",
                currentDate.getDayOfMonth(),
                currentDate.getMonthValue(),
                currentDate.getYear());
    }

    /**
     * Sets the current time as the ban time.
     */
    public void setBanTime() {
        LocalDateTime currentTime = LocalDateTime.now();
        bantime = String.format("%02d:%02d:%02d",
                currentTime.getHour(),
                currentTime.getMinute(),
                currentTime.getSecond());
    }

    /**
     * Converts the `PlayerData` object to a JSON string representation.
     *
     * @return The JSON representation of the `PlayerData` object.
     */
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

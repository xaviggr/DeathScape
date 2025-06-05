package dc.Business.controllers;

import dc.Business.player.PlayerData;
import dc.DeathScape;
import dc.Persistence.config.MainConfigManager;
import dc.Persistence.player.PlayerDatabase;
import dc.Persistence.player.PlayerEditDatabase;
import dc.Persistence.stash.PlayerStashDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Manages server-related functionalities, including player queues, server day tracking,
 * storm time management, and promotion messages for the DeathScape plugin.
 */
public class ServerController {

    private final File configFile;
    private final FileConfiguration config;
    private boolean rainTaskActive;
    private final DeathScape plugin;

    private int playerCounter = 0; // Counter for active players
    private final Queue<Player> queue = new LinkedList<>(); // Queue of players
    public static List<Player> SleepingPlayers = new ArrayList<>(); // List of players currently sleeping
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final List<String> TIMESTAMP_KEYS = Arrays.asList("last_update", "season_start", "next_check");

    /**
     * Constructor to initialize the ServerController with the required plugin and server data configurations.
     *
     * @param plugin The main instance of the DeathScape plugin.
     */
    public ServerController(DeathScape plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "serverData.yml");
        config = YamlConfiguration.loadConfiguration(configFile);

        // Initialize default values if not present
        if (!config.isSet("last_update")) {
            config.set("last_update", Instant.now().toEpochMilli());
        }
        if (!config.isSet("storm_time_pending")) {
            config.set("storm_time_pending", 0);
        }
        saveConfig();

        rainTaskActive = false;
        startDayUpdater();
        startPromotionMessages();
    }

    // --------------------------------------
    // SERVER DAY MANAGEMENT
    // --------------------------------------

    /**
     * Starts a task to update server days automatically every 24 hours.
     */
    public void startDayUpdater() {
        ZoneId zoneId = ZoneId.of("Europe/Madrid");

        migrateTimestampsToISO(zoneId); // ← Migramos si hay datos antiguos

        if (!config.contains("last_update")) {
            ZonedDateTime now = ZonedDateTime.now(zoneId);
            ZonedDateTime firstCheckpoint = now.withHour(19).withMinute(0).withSecond(0).withNano(0);
            if (now.getHour() >= 19) firstCheckpoint = firstCheckpoint.plusDays(1);

            String isoTime = firstCheckpoint.withZoneSameInstant(ZoneOffset.UTC).format(ISO_FORMATTER);
            config.set("last_update", isoTime);
            config.set("season_start", isoTime);
            config.set("server_days", 0);
            saveConfig();

            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Inicio de temporada registrado: " + firstCheckpoint);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> scheduleDayUpdaterAt19(zoneId), calculateDelayTo19(zoneId));
    }

    /**
     * Calcula el tiempo en ticks hasta las 19:00 hora local.
     */
    private long calculateDelayTo19(ZoneId zoneId) {
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime next19 = now.getHour() >= 19
                ? now.plusDays(1).withHour(19).withMinute(0).withSecond(0).withNano(0)
                : now.withHour(19).withMinute(0).withSecond(0).withNano(0);

        long delayMillis = Duration.between(now, next19).toMillis();
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Siguiente actualización de día en " + TimeUnit.MILLISECONDS.toMinutes(delayMillis) + " minutos.");
        return TimeUnit.MILLISECONDS.toSeconds(delayMillis) * 20;
    }

    /**
     * Programa la ejecución diaria a las 19:00 hora local.
     */
    private void scheduleDayUpdaterAt19(ZoneId zoneId) {
        checkDay(zoneId);
        long delay = calculateDelayTo19(zoneId);
        Bukkit.getScheduler().runTaskLater(plugin, () -> scheduleDayUpdaterAt19(zoneId), delay);
    }

    /**
     * Updates the number of server days based on the time elapsed since the last update.
     */
    public void checkDay(ZoneId zoneId) {
        String lastUpdateRaw = config.getString("last_update");

        ZonedDateTime lastUpdate;
        try {
            lastUpdate = ZonedDateTime.parse(lastUpdateRaw);
        } catch (DateTimeParseException e1) {
            try {
                long millis = Long.parseLong(lastUpdateRaw);
                lastUpdate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId);
                String isoFormatted = lastUpdate.withZoneSameInstant(ZoneOffset.UTC).format(ISO_FORMATTER);
                config.set("last_update", isoFormatted);
                saveConfig();
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Migrado 'last_update' a ISO: " + isoFormatted);
            } catch (NumberFormatException e2) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error al interpretar 'last_update': " + lastUpdateRaw);
                return;
            }
        }

        lastUpdate = lastUpdate.withZoneSameInstant(zoneId);
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Última actualización registrada: " + lastUpdate);

        if (lastUpdate.getHour() != 19) {
            lastUpdate = lastUpdate.withHour(19).withMinute(0).withSecond(0).withNano(0);
            if (lastUpdate.isAfter(now)) {
                lastUpdate = lastUpdate.minusDays(1);
            }
        }

        long daysElapsed = Duration.between(lastUpdate, now).toDays();
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "Días transcurridos desde última actualización: " + daysElapsed);

        if (daysElapsed > 0) {
            int currentDays = config.getInt("server_days", 0);
            int updatedDays = currentDays + (int) daysElapsed;

            if (updatedDays >= 30) {
                updatedDays = 0;
                Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "¡Temporada completada! Reiniciando días a 0.");

                for (Player online : Bukkit.getOnlinePlayers()) {
                    List<ItemStack> stash = PlayerStashDatabase.getStash(online.getName());
                    int given = 0;

                    for (ItemStack item : stash) {
                        if (item != null && item.getType() != Material.AIR) {
                            online.getInventory().addItem(item);
                            given++;
                        }
                    }

                    if (given > 0) {
                        online.sendMessage(ChatColor.GOLD + "Has recuperado " + given + " objetos de tu alijo anterior.");
                    }

                    PlayerStashDatabase.setStash(online.getName(), Arrays.asList(null, null, null, null));
                }

            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Días del servidor actualizados a: " + updatedDays);
            }

            config.set("server_days", updatedDays);

            ZonedDateTime effectiveUpdate = lastUpdate.plusDays(daysElapsed);
            String isoEffective = effectiveUpdate.withZoneSameInstant(ZoneOffset.UTC).format(ISO_FORMATTER);
            config.set("last_update", isoEffective);

            ZonedDateTime nextCheckpoint = now.withHour(19).withMinute(0).withSecond(0).withNano(0);
            if (now.getHour() >= 19) nextCheckpoint = nextCheckpoint.plusDays(1);
            String isoNext = nextCheckpoint.withZoneSameInstant(ZoneOffset.UTC).format(ISO_FORMATTER);
            config.set("next_check", isoNext);

            saveConfig();
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Actualización completada. Última a: " + effectiveUpdate);
        } else {
            Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "No han pasado días completos desde la última actualización.");
        }
    }

    private void migrateTimestampsToISO(ZoneId zoneId) {
        for (String key : TIMESTAMP_KEYS) {
            if (!config.contains(key)) continue;

            String value = config.getString(key);
            if (value == null) continue;

            // Si ya está en formato ISO, lo dejamos
            try {
                ZonedDateTime.parse(value);
                continue;
            } catch (DateTimeParseException ignored) {}

            // Intentamos convertir desde timestamp en milisegundos
            try {
                long millis = Long.parseLong(value);
                ZonedDateTime dt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId);
                String iso = dt.withZoneSameInstant(ZoneOffset.UTC).format(ISO_FORMATTER);
                config.set(key, iso);
                Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Migrado '" + key + "' a ISO: " + iso);
            } catch (NumberFormatException e) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "No se pudo migrar '" + key + "' con valor: " + value);
            }
        }

        saveConfig();
    }

    /**
     * Gets the current number of server days.
     *
     * @return The number of server days.
     */
    public int getServerDays() {
        return config.getInt("server_days");
    }

    /**
     * Sets the current number of server days.
     *
     * @param days The number of days to set.
     */
    public void setServerDays(int days) {
        config.set("server_days", days);
        saveConfig();
    }

    // --------------------------------------
    // PROMOTION MESSAGES
    // --------------------------------------

    /**
     * Starts a task to send periodic promotional messages in the chat.
     */
    private void startPromotionMessages() {
        List<String> messages = new ArrayList<>();
        messages.add(ChatColor.GOLD + "[Store]: " + ChatColor.YELLOW + "Visit our store to enhance your experience! " + ChatColor.AQUA + "Use /store for more information.");
        messages.add(ChatColor.GREEN + "[Events]: " + ChatColor.LIGHT_PURPLE + "Don't miss the weekly events! " + ChatColor.WHITE + "Check the calendar with " + ChatColor.AQUA + "/events.");
        messages.add(ChatColor.BLUE + "[Store]: " + ChatColor.YELLOW + "Exclusive kits are now available! " + ChatColor.AQUA + "Check them out in the store with /store.");
        messages.add(ChatColor.RED + "[Events]: " + ChatColor.GOLD + "Participate in tournaments and win amazing prizes! " + ChatColor.WHITE + "Register now with " + ChatColor.AQUA + "/tournaments.");
        messages.add(ChatColor.DARK_GREEN + "[Store]: " + ChatColor.YELLOW + "Special offers this month! Don't miss out on your favorite items. /store");

        int[] currentIndex = {0};

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String message = messages.get(currentIndex[0]);
            Bukkit.broadcastMessage(message);
            currentIndex[0] = (currentIndex[0] + 1) % messages.size();
        }, 0L, 20 * 60 * 20L); // Every 20 minutes
    }

    // --------------------------------------
    // STORM TIME MANAGEMENT
    // --------------------------------------

    /**
     * Gets the amount of pending storm time in minutes.
     *
     * @return The pending storm time in minutes.
     */
    public int getStormPendingTime() {
        return config.getInt("storm_time_pending");
    }

    /**
     * Adds time to the pending storm duration.
     *
     * @param minutes The number of minutes to add.
     */
    public void addStormTime(int minutes) {
        int currentTime = config.getInt("storm_time_pending");
        config.set("storm_time_pending", currentTime + minutes);
        saveConfig();
    }

    /**
     * Reduces the pending storm duration by a specified number of minutes.
     *
     * @param minutes The number of minutes to reduce.
     */
    public void reduceStormTime(int minutes) {
        int currentTime = config.getInt("storm_time_pending");
        int newTime = Math.max(0, currentTime - minutes);
        config.set("storm_time_pending", newTime);
        saveConfig();
    }

    // --------------------------------------
    // PLAYER QUEUE MANAGEMENT
    // --------------------------------------

    /**
     * Checks the player queue and teleports players into the world if there is space available.
     */
    public void checkQueueStatus() {
        int maxPlayersInWorld = MainConfigManager.getInstance().getMaxPlayersInWorld();
        if (!queue.isEmpty() && playerCounter < maxPlayersInWorld) {
            Player player = queue.poll();
            Location location = PlayerEditDatabase.getPlayerLocation(Objects.requireNonNull(player).getName());
            if (location == null) {
                player.kickPlayer("Error loading your location. Please contact an administrator.");
                return;
            }
            player.teleport(location);
            PlayerData playerData = PlayerDatabase.getPlayerDataFromDatabase(player.getName());
            assert playerData != null;
            player.setHealth(playerData.getHealth());
            increasePlayerCounter();
        }
    }

    /**
     * Increases the active player counter.
     */
    public void increasePlayerCounter() {
        playerCounter++;
    }

    /**
     * Decreases the active player counter.
     */
    public void decreasePlayerCounter() {
        playerCounter--;
    }

    /**
     * Gets the current active player count.
     *
     * @return The active player count.
     */
    public int getPlayerCounter() {
        return playerCounter;
    }

    /**
     * Adds a player to the queue and checks the queue status.
     *
     * @param player The player to add to the queue.
     */
    public void addPlayerToQueue(Player player) {
        queue.add(player);
        checkQueueStatus();
    }

    /**
     * Removes a player from the queue and checks the queue status.
     *
     * @param player The player to remove from the queue.
     */
    public void removePlayerFromQueue(Player player) {
        queue.remove(player);
        checkQueueStatus();
    }

    /**
     * Gets the position of a player in the queue.
     *
     * @param player The player to check.
     * @return The queue position of the player.
     */
    public int getQueuePosition(Player player) {
        return new ArrayList<>(queue).indexOf(player) + 1;
    }

    // --------------------------------------
    // CONFIGURATION MANAGEMENT
    // --------------------------------------

    /**
     * Saves the server configuration to the file.
     */
    private void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if the rain task is currently active.
     *
     * @return True if the rain task is active, false otherwise.
     */
    public boolean isRainTaskActive() {
        return rainTaskActive;
    }

    /**
     * Sets the rain task status to active or inactive.
     *
     * @param isActive True to activate the rain task, false to deactivate it.
     */
    public void setRainTaskActive(boolean isActive) {
        this.rainTaskActive = isActive;
    }
}

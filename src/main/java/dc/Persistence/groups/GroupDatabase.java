package dc.Persistence.groups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dc.Business.groups.GroupData;
import dc.Persistence.config.MainConfigManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A database class for managing groups and their associated data.
 * Provides methods to load, write, and manipulate group data stored in a JSON file.
 */
public class GroupDatabase {

    private static String nameFile; // Path to the file storing group data
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create(); // Gson instance for JSON operations

    /**
     * Sets the file name for the group database.
     *
     * @param nameFile The file name to be used for storing groups.
     */
    public static void setNameFile(String nameFile) {
        GroupDatabase.nameFile = nameFile;
    }

    /**
     * Loads the group data from the JSON file.
     *
     * @return A JsonObject containing all the group data.
     */
    private static JsonObject loadGroups() {
        File file = new File(nameFile);
        if (!file.exists()) {
            initGroupDatabase();
            return new JsonObject();
        }

        try (FileReader reader = new FileReader(file)) {
            return GSON.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }

    /**
     * Writes the given group data to the JSON file.
     *
     * @param groupsData The JsonObject containing group data to be written.
     */
    private static void writeGroups(JsonObject groupsData) {
        try (FileWriter writer = new FileWriter(nameFile)) {
            writer.write(GSON.toJson(groupsData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the group database by creating the file and populating it with default groups.
     */
    public static void initGroupDatabase() {
        File file = new File(nameFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                JsonObject defaultGroupsData = generateGroupsFromConfig();
                writeGroups(defaultGroupsData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates groups from the configuration file and converts them to JSON format.
     *
     * @return A JsonObject containing all groups defined in the configuration.
     */
    private static JsonObject generateGroupsFromConfig() {
        JsonObject groupsData = new JsonObject();

        // Retrieve groups from the config.yml file via MainConfigManager
        List<GroupData> groups = MainConfigManager.getInstance().getGroupsFromConfig();
        for (GroupData group : groups) {
            groupsData.add(group.getName(), GSON.toJsonTree(group));
        }

        return groupsData;
    }

    /**
     * Adds a new group to the database.
     *
     * @param groupData The GroupData object representing the group to be added.
     */
    public static void addGroupData(GroupData groupData) {
        JsonObject groupsData = loadGroups();
        groupsData.add(groupData.getName(), GSON.toJsonTree(groupData));
        writeGroups(groupsData);
    }

    /**
     * Retrieves a group's data by its name.
     *
     * @param groupName The name of the group.
     * @return A GroupData object containing the group's data, or null if not found.
     */
    public static GroupData getGroupData(String groupName) {
        JsonObject groupsData = loadGroups();
        JsonObject groupObject = groupsData.getAsJsonObject(groupName);

        if (groupObject != null) {
            return GSON.fromJson(groupObject, GroupData.class);
        }
        return null;
    }

    /**
     * Retrieves a list of all group names.
     *
     * @return A list of strings representing the names of all groups.
     */
    public static List<String> getAllGroups() {
        return new ArrayList<>(loadGroups().keySet());
    }

    /**
     * Retrieves the prefix associated with a given group.
     *
     * @param groupName The name of the group.
     * @return The prefix string for the group, or null if the group is not found.
     */
    public static String getPrefixFromGroup(String groupName) {
        GroupData groupData = getGroupData(groupName);
        return groupData != null ? groupData.getPrefix() : null;
    }
}
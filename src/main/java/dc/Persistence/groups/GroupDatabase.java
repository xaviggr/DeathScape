package dc.Persistence.groups;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dc.Business.groups.GroupData;
import dc.Persistence.config.MainConfigManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDatabase {

    private static String nameFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void setNameFile(String nameFile) {
        GroupDatabase.nameFile = nameFile;
    }

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

    private static void writeGroups(JsonObject groupsData) {
        try (FileWriter writer = new FileWriter(nameFile)) {
            writer.write(GSON.toJson(groupsData));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initGroupDatabase() {
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

    private static JsonObject generateGroupsFromConfig() {
        JsonObject groupsData = new JsonObject();

        // Llama al método loadGroupsFromConfig de MainConfigManager para obtener los grupos desde config.yml
        List<GroupData> groups = MainConfigManager.getInstance().getGroupsFromConfig();
        for (GroupData group : groups) {
            groupsData.add(group.getName(), GSON.toJsonTree(group));
        }

        return groupsData;
    }

    public static boolean addGroupData(GroupData groupData) {
        JsonObject groupsData = loadGroups();
        groupsData.add(groupData.getName(), GSON.toJsonTree(groupData));
        writeGroups(groupsData);
        return true;
    }

    public static GroupData getGroupData(String groupName) {
        JsonObject groupsData = loadGroups();
        JsonObject groupObject = groupsData.getAsJsonObject(groupName);

        if (groupObject != null) {
            return GSON.fromJson(groupObject, GroupData.class);
        }
        return null;
    }

    public static List<String> getAllGroups() {
        return new ArrayList<>(loadGroups().keySet());
    }
}
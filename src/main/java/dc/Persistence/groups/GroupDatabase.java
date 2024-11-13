package dc.Persistence.groups;

import dc.Persistence.player.PlayerDatabase;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class GroupDatabase {

    private static String nameFile;

    public static void setNameFile(String nameFile) {
        GroupDatabase.nameFile = nameFile;
    }

    public static void initPlayerDatabase() {
        File file = new File(nameFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

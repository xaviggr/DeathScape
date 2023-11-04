package dc.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Search {
    public static boolean buscarPalabraProhibida(String wordToFind) {
        boolean encontrada = false;
        File archivoPalabrasProhibidas = new File("plugins/DeathScape/palabrasprohibidas.txt");

        try {
            if (!archivoPalabrasProhibidas.exists()) {
                archivoPalabrasProhibidas.createNewFile(); // Crea el archivo si no existe
            }

            Scanner myReader = new Scanner(archivoPalabrasProhibidas);
            while (myReader.hasNextLine()) {
                String linea = myReader.nextLine();
                String[] palabras = linea.split(",");
                for (String palabra : palabras) {
                    if (palabra.trim().equalsIgnoreCase(wordToFind)) {
                        encontrada = true;
                        break;
                    }
                }
                if (encontrada) {
                    break;
                }
            }
            myReader.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Aquí puedes manejar la excepción según tu requerimiento, como registrar un error o notificar al usuario.
        }

        return encontrada;
    }
}

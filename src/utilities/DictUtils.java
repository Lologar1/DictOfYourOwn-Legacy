package utilities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class DictUtils {
    public static AbstractMap loadFromFile(Path path) {
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (AbstractMap) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            System.err.println("Couldn't find file " + path.getFileName() + ", creating empty.");
            return null;
        }
    }

    public static void importFile(Path file, TreeMap<String, List<String>> dict, HashMap<String, HashSet<String>> tags,
                                  HashMap<String, String[]> words) throws IOException {
        if (!Files.exists(file)) {
            System.err.println("File " + file.getFileName() + " doesn't exist !");
            return;
        }
        String key = file.getFileName().toString().strip().split("\\.")[0];
        System.out.println("Importing file under key " + key);
        List<String> lines = Files.readAllLines(file);
        String[] wordTags = lines.removeFirst().split(", ");
        System.out.println("Adding to tags : " + Arrays.toString(wordTags));

        dict.put(key, lines); //Set definition

        //Add to tags
        for (String tag : wordTags) {
            if (!tags.containsKey(tag)) {
                tags.put(tag, new HashSet<>());
            }
            tags.get(tag).add(key);
        }

        //Add tags to word
        words.put(key, wordTags);
    }

    public static void saveToFile(Path path, Object object) {
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } catch (IOException e) {
            System.err.println("Failed to write object to path " + path.getFileName());
            throw new RuntimeException(e);
        }
    }

    public static void writeToFile(String path, ArrayList<String> lines) {
        try (FileWriter writer = new FileWriter(path)) {
            // Write each element of the ArrayList to the file
            for (String str : lines) {
                writer.write(str);
                writer.write("\n"); // Add a newline after each item
            }
        } catch (IOException e) {
            System.err.println("Couldn't save dictionary to file " + path);
        }
    }
}

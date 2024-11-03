import utilities.DictUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static utilities.DictUtils.*;

public class Main {
    public static final float version = 0.2f;
    public static final Path mainPath = Paths.get("DictOfYourOwn");
    public static final Path dictPath = mainPath.resolve("dictionary");
    public static final Path tagsPath = mainPath.resolve("tags");
    public static final Path wordsPath = mainPath.resolve("words");

    public static TreeMap<String, List<String>> dictionary = new TreeMap<>();
    public static HashMap<String, HashSet<String>> tags = new HashMap<>();
    public static HashMap<String, String[]> words = new HashMap<>(); //Reverse of tags, used in deletion to avoid iterating over tags.

    public static void main(String[] args) throws IOException {
        System.out.println("Starting DoYO version " + version);

        if (!Files.exists(mainPath)) {
            System.out.println("Main directory missing. Initializing main directory under " + mainPath.getFileName());
            Files.createDirectory(mainPath);

        } else {
            //Get appropriate hashmaps back.
            dictionary = loadFromFile(dictPath) == null ? new TreeMap<>() : (TreeMap<String, List<String>>) loadFromFile(dictPath);
            tags = loadFromFile(tagsPath) == null ? new HashMap<>() : (HashMap<String, HashSet<String>>) loadFromFile(tagsPath);
            words = loadFromFile(wordsPath) == null ? new HashMap<>() : (HashMap<String, String[]>) loadFromFile(wordsPath);
        }

        //Some useful information.
        displayInfo();

        //Main loop
        Scanner rawInput = new Scanner(System.in);
        while (true) {
            System.out.print(">");
            String[] input = rawInput.nextLine().strip().toLowerCase().split(" ");
            String command = input[0];
            switch (command) {
                case "import" -> {
                    importFile(Paths.get(input[1]), dictionary, tags, words);
                    saveState();
                }
                case "importall" -> {
                    try (Stream<Path> files = Files.list(Paths.get(""))) {
                        files.filter(f -> f.getFileName().toString().endsWith(".txt"))
                                .forEach(f -> {
                                    try {
                                        importFile(f, dictionary, tags, words);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                    }
                    saveState();
                }
                case "remove" -> {
                    removeFile(input[1]);
                    saveState();
                }
                case "list" -> System.out.println("Matching words : " + tags.get(input[1]));
                case "search" -> System.out.println(dictionary.keySet().stream().filter(w -> w.contains(input[1])).toList());
                case "regex" -> {
                    try {
                        System.out.println(dictionary.keySet().stream().filter(w -> w.matches(input[1])).toList());
                    } catch (PatternSyntaxException e) {
                        System.err.println("Bad regex.");
                    }
                }
                case "desc" -> System.out.println(dictionary.keySet().stream().filter(w -> String.join("\n", dictionary.get(w)).contains(input[1])).toList());
                case "view" -> {
                    String word = input[1];
                    if (!dictionary.containsKey(word)) {
                        System.out.println("Undefined word.");
                    } else {
                        System.out.println("=== " + word + " ===");
                        System.out.println("Tags : " + Arrays.toString(words.get(word)));
                        System.out.println();
                        dictionary.get(word).forEach(System.out::println);
                    }
                }
                case "export" -> {
                    ArrayList<String> dict = new ArrayList<>();
                    for (String word : dictionary.keySet()) {
                        dict.add("=== " + word + " ===");
                        dict.add("Tags : " + Arrays.toString(words.get(word)));
                        dict.add("");
                        dict.addAll(dictionary.get(word));
                        dict.add("");
                    }
                    writeToFile(input[1], dict);
                    System.out.println("Dictionary exported under " + input[1]);
                }
                case "help" -> displayInfo();
                case "exit" -> {
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help' for help.");
            }
        }
    }

    public static void displayInfo() {
        System.out.println("Avaliable commands : import, remove, export");
        System.out.println();
        System.out.println("Import syntax : import [WORD].txt or importall");
        System.out.println("Will add the specified file(s) to the dictionary. The first line should contain a comma-separated list of tags.");
        System.out.println();
        System.out.println("Remove syntax : remove [WORD]");
        System.out.println("Will remove the word from the dictionary. This action cannot be undone.");
        System.out.println();
        System.out.println("Export syntax : export [DICTNAME]");
        System.out.println("Will export this dictionary as a text file, ordered alphabetically.");
        System.out.println();
        System.out.println("Searching syntax : list [TAG] or search [SUBSTRING] or regex [REGEX] or desc [SUBSTRING]");
        System.out.println("Will list all words with this tag, or all words matching this substring/regex, or all words with a description matching the substring.");
        System.out.println();
        System.out.println("Viewing syntax : view [WORD]");
        System.out.println("Will display this word's definition and tags.");
        System.out.println();
        System.out.println("Exit to quit.");
    }

    public static void removeFile(String entry) {
        if (!dictionary.containsKey(entry)) {
            System.out.println("No such entry to remove !");
            return;
        }
        dictionary.remove(entry);
        for (String tag : words.get(entry)) {
            tags.get(tag).remove(entry);
            if (tags.get(tag).isEmpty()) {
                tags.remove(tag);
            }
        }
        words.remove(entry);
        System.out.println("Successfully removed entry " + entry);
    }

    public static void saveState() {
        saveToFile(dictPath, dictionary);
        saveToFile(tagsPath, tags);
        saveToFile(wordsPath, words);
    }
}
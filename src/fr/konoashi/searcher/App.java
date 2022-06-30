package fr.konoashi.searcher;

import com.google.gson.JsonElement;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


public class App {

    public static LinkedList<JsonElement> datacheck = new LinkedList<>();

    String API_KEYS_FILE = "keys.txt";
    static ArrayList<String> keys = new ArrayList<>();

    // Map<apiKey, lastUsed>
    static ConcurrentHashMap<UUID, Integer> keyToUsage
            = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new App();
    }

    public App() {
        // insert keys in the map
        try {
            for (String key: getKeys(API_KEYS_FILE)) {
                keyToUsage.put(UUID.fromString(key), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(keyToUsage);
        // TODO: check if keys are valid

        //Data Consumer & Producer
        var producerConsumer = new ProducerConsumer();
        try {
            producerConsumer.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // get apis keys from a txt file
    private ArrayList<String> getKeys(String filename) throws IOException {

        ArrayList<String> keys = new ArrayList<>();

        //read the file
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                keys.add(line);
                App.keys.add(line);
            }
        }

        return keys;
    }

}

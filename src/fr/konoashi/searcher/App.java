package fr.konoashi.searcher;

import java.io.*;
import java.security.Key;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class App {

    String API_KEYS_FILE = "keys.txt";

    // Map<apiKey, lastUsed>
    ConcurrentHashMap<String, Long> keyMap
            = new ConcurrentHashMap<>();

    public static void main(String[] args) {
//        ConcurrentHashMap<UUID, Integer> m
//                = new ConcurrentHashMap<>();
//
//        for (int i = 1; i < keys.length+1; i++) {
//            m.put(UUID.fromString(keys[i-1]), 0);
//        }
//
//        var producerConsumer = new ProducerConsumer();
//        try {
//            producerConsumer.run(5, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        new App();
    }

    public App() {
        // insert keys in the map
        try {
            for (String key: getKeys(API_KEYS_FILE)) {
                keyMap.put(key, System.currentTimeMillis());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: check if keys are valid
    }

    // get apis keys from a txt file
    private ArrayList<String> getKeys(String filename) throws IOException {

        ArrayList<String> keys = new ArrayList<>();

        //read the file
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            for(String line; (line = br.readLine()) != null; ) {
                keys.add(line);
            }
        }

        return keys;
    }

}

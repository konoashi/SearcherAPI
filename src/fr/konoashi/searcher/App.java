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

    // get apis keys from a txt file
    public ArrayList<String> getKeys() throws IOException {

        ArrayList<String> keys = new ArrayList<>();

        //read the file
        try(BufferedReader br = new BufferedReader(new FileReader(API_KEYS_FILE))) {
            for(String line; (line = br.readLine()) != null; ) {
                keys.add(line);
            }
        }

        return keys;
    }

    public App() {
        // insert keys in the map
        try {
            for (String key: getKeys()) {
                keyMap.put(key, System.currentTimeMillis());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // TODO: check if keys are valid
    }

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
}

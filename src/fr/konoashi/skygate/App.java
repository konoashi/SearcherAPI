package fr.konoashi.skygate;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


public class App {

    String API_KEYS_FILE = "keys.txt";

    // Map<apiKey, lastUsed>
    static ConcurrentHashMap<String, Integer> keyToUsage
            = new ConcurrentHashMap<>();

    public static ProducerConsumer producerConsumer;

    public static void main(String[] args) {
        new App();
    }

    public App() {
        // insert keys in the map
        try {
            for (String key: getKeys(API_KEYS_FILE)) {
                keyToUsage.put(key, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(keyToUsage);
        // TODO: check if keys are valid

        //Data Consumer & Producer
        producerConsumer = new ProducerConsumer();
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
            }
        }

        return keys;
    }

}

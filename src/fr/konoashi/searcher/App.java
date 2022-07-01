package fr.konoashi.searcher;

import com.google.gson.JsonElement;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;


public class App {

    String API_KEYS_FILE = "keys.txt";

    // Map<apiKey, lastUsed>
    static ConcurrentHashMap<String, Integer> keyToUsage
            = new ConcurrentHashMap<>();

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

        /*new Thread(() -> {
            while (ProducerConsumer.isRunning) {
                for (Map.Entry<String, Integer> entry : keyToUsage.entrySet()) {
                    keyToUsage.replace(entry.getKey(), 0);
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();*/

        System.out.println(keyToUsage);
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
            }
        }

        return keys;
    }

}

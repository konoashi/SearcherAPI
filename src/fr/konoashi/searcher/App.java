package fr.konoashi.searcher;

import com.google.gson.JsonElement;

import java.io.*;
import java.net.http.HttpClient;
import java.security.Key;
import java.util.*;
import java.util.concurrent.*;


public class App {

    public static LinkedList<JsonElement> datacheck = new LinkedList<>();
    //public static BlockingQueue<JsonElement> datacheck = new LinkedBlockingQueue<>();
    String API_KEYS_FILE = "keys.txt";
    static ArrayList<String> keys = new ArrayList<>();

    // Map<apiKey, lastUsed>
    static ConcurrentHashMap<UUID, Integer> keyMap
            = new ConcurrentHashMap<>();

    static ConcurrentHashMap<UUID, Integer> m
            = new ConcurrentHashMap<>();
    public static void main(String[] args) {

        new App();
    }

    public App() {
        // insert keys in the map
        try {
            for (String key: getKeys(API_KEYS_FILE)) {
                keyMap.put(UUID.fromString(key), 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(keyMap);
        // TODO: check if keys are valid

        //Send Data to an API when it's full
        new Thread(() -> {
            while (true) {
                if(datacheck.size() > 99) {
                    LinkedList<JsonElement> dataPut = new LinkedList<>();
                    for (int i = 0; i < 100; i++) {
                        //System.out.println(datacheck.get(0));
                        dataPut.add(datacheck.get(0));
                        datacheck.remove(0);
                    }
                    //TODO: MONGO REQUEST

                }
            }
        }).start();

        //Data Consumer & Producer
        var producerConsumer = new ProducerConsumer();
        try {
            producerConsumer.run(120, TimeUnit.SECONDS);
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

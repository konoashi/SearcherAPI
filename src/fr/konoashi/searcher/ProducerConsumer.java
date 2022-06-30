package fr.konoashi.searcher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static fr.konoashi.searcher.App.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

public class ProducerConsumer {

    final int API_KEY_LIMIT = 119;
    final int MONGO_BATCH_SIZE = 100;
    final int THREADS = 6;

    final Gson gson = new Gson();

    private final BlockingQueue<String> uuids = new LinkedBlockingQueue<>();
    private final BlockingQueue<JsonObject> items = new LinkedBlockingQueue<>();

    boolean isRunning = true;

    public ProducerConsumer() {

        try {
            try(BufferedReader br = new BufferedReader(new FileReader("./uuids.txt"))) {
                for(String line; (line = br.readLine()) != null; ) {
                    try {
                        uuids.put(line);
                    } catch (Exception e) {
                        System.err.println("Error while adding a uuid to the queue");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//        //Friendlist à ajouter ici
//        for (int i = 0; i < 1; i++) {
//            var dataUnit = "2103bf99-d770-436f-9c9b-c235d55f819f";
//            try {
//                uuids.put(dataUnit);
//            } catch (Exception e) {
//                System.err.println("Error while adding a uuid to the queue");
//                e.printStackTrace();
//            }
//        }
    }

    private final Callable<Void> consumer = () -> {
        ArrayList<JsonObject> itemsToInsert = new ArrayList<>();

        while (isRunning) {
            try {
                JsonObject item = items.poll(1, TimeUnit.SECONDS);
                if (item == null) {
                    continue;
                }
                itemsToInsert.add(item);
                if (itemsToInsert.size() == MONGO_BATCH_SIZE) {
                    //TODO: MONGO REQUEST
                    System.out.println("[LOG] Consumed " + itemsToInsert.size() + " items, " + uuids.size() + " uuids left");
                    itemsToInsert.clear();
                }
            } catch (Exception e) {
                System.err.println("Error while getting an item");
                e.printStackTrace();
            }
        }

        System.out.println("[LOG] Consumer stopped");
        return null;
    };

    private final Callable<Void> producer = () -> {

        while (isRunning) {
            //TODO: Reset key loop
            Iterator<String> keyIter = keyToUsage.keySet().iterator();
            String key = keyIter.next();
            while (keyToUsage.get(key) >= API_KEY_LIMIT && keyIter.hasNext()) {
                key = keyIter.next();
            }
            if (keyToUsage.get(key) > API_KEY_LIMIT) {
                System.err.println("[ERROR] No more keys available");
                System.err.println(keyToUsage);
                Thread.sleep(1000);
                continue;
            }
            //Increment by 1 the key usage
            keyToUsage.merge(key, 1, Integer::sum);

            Reader  rd;
            StringBuilder sb = new StringBuilder();
            var uuid = uuids.poll(1, TimeUnit.SECONDS);
            if (uuid == null) {
                System.out.println("[LOG] No uuid left, stopping");
                isRunning = false;
                break;
            }

            System.out.println(
                    "Consumed: " + uuid +
                    " | usage: " + keyToUsage.get(key) +
                    " key: " + key +
                    " from " + Thread.currentThread().getName()
            );

            URL url = new URL("https://api.hypixel.net/skyblock/profiles?key=" + key +"&uuid=" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.connect();

            if ("gzip".equals(con.getContentEncoding())) {
                rd = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
            }
            else {
                rd = new InputStreamReader(con.getInputStream());
            }

            while (true) {
                int ch = rd.read();
                if (ch==-1) {
                    break;
                }
                sb.append((char)ch);
            }

//            System.out.println(sb);
            System.out.println("[LOG] Got response from API");

            JsonObject profilesEndpointJson = gson.fromJson(sb.toString(), JsonObject.class);
            ArrayList<JsonObject> profilesItems = Searcher.getProfilesItems(profilesEndpointJson);


            if (profilesItems == null) {
                continue;
            }

            items.addAll(profilesItems);
        }

        System.out.println("[LOG] Producer stopped");
        return null;
    };

    public void run() throws InterruptedException {
        var pool = Executors.newCachedThreadPool();
        for (int i = 0; i < THREADS; i++) {
            pool.submit(producer);
        }
        pool.submit(consumer);
        pool.shutdown();
        //pool.awaitTermination(forHowLong, unit);
    }
}

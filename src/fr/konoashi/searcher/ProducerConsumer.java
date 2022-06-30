package fr.konoashi.searcher;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static fr.konoashi.searcher.App.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.*;

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
        while (isRunning) {
            if(items.size() > MONGO_BATCH_SIZE) {
                LinkedList<JsonObject> itemsToInsert = new LinkedList<>();
                items.drainTo(itemsToInsert, MONGO_BATCH_SIZE);


//                for (JsonObject item : items) {
//                    System.out.println(item);
//                }
                //TODO: MONGO REQUEST
            }
        }

        return null;
    };

    private final Callable<Void> producer = () -> {

        while (isRunning) {
            String key = keys.get(0);
            BufferedReader rd;
            StringBuilder sb = new StringBuilder();
            String line;
            var uuid = uuids.poll(1, TimeUnit.SECONDS);
            if (uuid == null) {
                isRunning = false;
                break;
            }

            for (String s : keys) {
                if (keyToUsage.get(UUID.fromString(key)) > API_KEY_LIMIT && !key.equals(s)) {
                    key = s;
                    break;
                }
            }
            if (keyToUsage.get(UUID.fromString(key)) > API_KEY_LIMIT) {
                System.out.println("No more keys available");
                continue;
            }
            keyToUsage.merge(UUID.fromString(key), 1, Integer::sum);

            System.out.println(
                    "Consumed: " + uuid +
                    " | usage: " + keyToUsage.get(UUID.fromString(key)) +
                    " key: " + key +
                    " from " + Thread.currentThread().getName()
            );

            URL url = new URL("https://api.hypixel.net/skyblock/profiles?key=" + key +"&uuid=" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = rd.readLine()) != null)
            {
                sb.append(line).append('\n');
            }

//            System.out.println(sb);

            JsonObject profilesEndpointJson = gson.fromJson(sb.toString(), JsonObject.class);
            ArrayList<JsonObject> profilesItems = Searcher.getProfilesItems(profilesEndpointJson);

            if (profilesItems == null) {
                continue;
            }

            items.addAll(profilesItems);

            //TODO: remove debug print
            for (JsonObject item : items) {
                System.out.println(item);
            }
        }

        return null;
    };

    public void run() throws InterruptedException {
        var pool = Executors.newCachedThreadPool();
        for (int i = 0; i < THREADS; i++) {
            pool.submit(producer);
        }
        pool.submit(consumer);

        //pool.submit(consumer);
        pool.shutdown();
        //pool.awaitTermination(forHowLong, unit);
    }
}

package fr.konoashi.searcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.*;

import static fr.konoashi.searcher.App.*;

public class ProducerConsumer {

    final int API_KEY_LIMIT = 119;

    private final BlockingQueue<String> uuids = new LinkedBlockingQueue<>();

    private final Callable<Void> consumer = () -> {
        while (true) {
            String key = keys.get(0);
            BufferedReader rd;
            StringBuilder sb = new StringBuilder();
            String line;
            var uuid = uuids.poll(5, TimeUnit.SECONDS);
            if (uuid == null)
                break;

            for (String s : keys) {
                if (keyMap.get(UUID.fromString(key)) > API_KEY_LIMIT && !key.equals(s)) {
                    key = s;
                    break;
                }
            }
            if (keyMap.get(UUID.fromString(key)) > API_KEY_LIMIT) {
                System.out.println("No more keys available");
                continue;
            }
            keyMap.merge(UUID.fromString(key), 1, Integer::sum);
            System.out.println("Consumed " + uuid + " | " + keyMap.get(UUID.fromString(key)) + " key "+ key + " from " + Thread.currentThread().getName());
            URL url = new URL("https://api.hypixel.net/skyblock/profiles?key=" + key +"&uuid=" + uuid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
            while ((line = rd.readLine()) != null)
            {
                sb.append(line).append('\n');
            }

            //CheckStuff(new JsonParser().parse(String.valueOf(sb)), dataUnit);
        }

        return null;
    };


    private final Callable<Void> producer = () -> {

        //Friendlist a ajouter ici
        for (int i = 0; i < 1; i++) {
            var dataUnit = "2103bf99-d770-436f-9c9b-c235d55f819f";
            uuids.put(dataUnit);
        }
        return null;
    };

    public void run(long forHowLong, TimeUnit unit) throws InterruptedException {
        var pool = Executors.newCachedThreadPool();
        pool.submit(producer);
        pool.submit(consumer);
        //pool.submit(consumer);
        //pool.shutdown();
        //pool.awaitTermination(forHowLong, unit);
    }


}

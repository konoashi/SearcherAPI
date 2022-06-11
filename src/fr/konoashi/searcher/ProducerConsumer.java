package fr.konoashi.searcher;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;

public class ProducerConsumer {

    private BlockingQueue<String> data = new LinkedBlockingQueue<>();

    private Callable<Void> consumer = () -> {
        while (true) {
            var dataUnit = data.poll(5, TimeUnit.SECONDS);
            if (dataUnit == null)
                break;
            System.out.println("Consumed " + dataUnit + " from " + Thread.currentThread().getName());
        }
        return null;
    };

    private Callable<Void> producer = () -> {

        //Friendlist a ajouter ici
        for (int i = 0; i < 90_000; i++) {
            var dataUnit = UUID.randomUUID().toString();
            data.put(dataUnit);
        }
        return null;
    };

    public void run(long forHowLong, TimeUnit unit) throws InterruptedException {
        var pool = Executors.newCachedThreadPool();
        pool.submit(producer);
        pool.submit(consumer);
        //pool.submit(consumer);
        pool.shutdown();
        pool.awaitTermination(forHowLong, unit);
    }

}

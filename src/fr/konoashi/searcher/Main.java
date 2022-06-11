package fr.konoashi.searcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class Main {
    public static String[] keys = {"7e57ef59-4c16-4f4a-ab7c-e095596b189b"};
    public static void main(String[] args) {
        ConcurrentHashMap<UUID, Integer> m
                = new ConcurrentHashMap<>();

        for (int i = 1; i < keys.length+1; i++) {
            m.put(UUID.fromString(keys[i-1]), 0);
        }

        var producerConsumer = new ProducerConsumer();
        try {
            producerConsumer.run(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}

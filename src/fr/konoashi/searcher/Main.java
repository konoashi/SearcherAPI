package fr.konoashi.searcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class Main {
    public static String[] keys = {"a74dfce5-f68c-4405-a0b6-c3607ad6f30b", "ea4095c4-3dcf-4676-9b37-f6756e99bd22"};
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

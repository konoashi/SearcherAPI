package fr.konoashi.searcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static fr.konoashi.searcher.App.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

public class ProducerConsumer {

    final int API_KEY_LIMIT = 119;
    final int MONGO_BATCH_SIZE = 1000;
    final int THREADS = 8;

    final Gson gson = new Gson();

    // HashMap<ID, HashMap<TIER, DefaultItemEntry>>
    private final HashMap<String, DefaultItemEntry> defaultItems = new HashMap<>();
    private final HashMap<String, DefaultPetEntry> defaultPets = new HashMap<>();
    final Searcher searcher = new Searcher(defaultItems, defaultPets);

    private final BlockingQueue<String> uuids = new LinkedBlockingQueue<>();
    private final BlockingQueue<JsonObject> items = new LinkedBlockingQueue<>();

    public static boolean isRunning = true;

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

        //Fill default items from hypixel's api
        String defaultItemsRespString;
        try {
            defaultItemsRespString = httpGet("https://api.hypixel.net/resources/skyblock/items");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while getting default items from hypixel's api");
        }
        JsonObject defaultItemsRespJson = gson.fromJson(defaultItemsRespString, JsonObject.class);

        for (JsonElement defaultItem : defaultItemsRespJson.getAsJsonArray("items")) {
            JsonObject defaultItemJson = defaultItem.getAsJsonObject();

            String material = (defaultItemJson.get("material") != null) ? defaultItemJson.get("material").getAsString() : null;
            String name = (defaultItemJson.get("name") != null) ? defaultItemJson.get("name").getAsString() : null;
            String tier = (defaultItemJson.get("tier") != null) ? defaultItemJson.get("tier").getAsString() : null;
            String id = (defaultItemJson.get("id") != null) ? defaultItemJson.get("id").getAsString() : null;
            String color = (defaultItemJson.get("color") != null) ? defaultItemJson.get("color").getAsString().replace(",", ":") : null;

            if (id == null) {
                continue;
            }
            if (Arrays.stream(Utils.excludeFromItemsApi).anyMatch(item -> item.equals(id)) || id.contains("CATACOMBS_PASS") || id.contains("BANNER")) {
                continue;
            }

            DefaultItemEntry defaultItemEntry = new DefaultItemEntry(material, name, tier, id, color);

            defaultItems.put(id, defaultItemEntry);
        }

        for (JsonElement defaultPet : Utils.petExclude.getAsJsonArray("pets")) {
            JsonObject defaultPetJson = defaultPet.getAsJsonObject();

            JsonArray tier = (defaultPetJson.get("rarities") != null) ? defaultPetJson.get("rarities").getAsJsonArray() : null;
            String type = (defaultPetJson.get("name") != null) ? defaultPetJson.get("name").getAsString() : null;
            if (tier == null || type == null) {
                continue;
            }
            String[] tierArray = new String[tier.size()];
            for(int i = 0; i < tier.size(); i++)
                tierArray[i] = tier.get(i).getAsString();

            DefaultPetEntry defaultItemEntry = new DefaultPetEntry(tierArray, type);

            defaultPets.put(type, defaultItemEntry);
        }

//        System.out.println(defaultItems);

        //TODO: Friendlist à ajouter ici
    }

    private void addUuidToQueue(String uuid) {
        try {
            uuids.put(uuid);
        } catch (Exception e) {
            System.err.println("Error while adding a uuid to the queue");
            e.printStackTrace();
        }
    }

    private void insertInMongo(MongoCollection<Document> collection, ArrayList<JsonObject> items) {
        ArrayList<Document> documents = new ArrayList<>();
        for (JsonObject item : items) {
            documents.add(Document.parse(gson.toJson(item)));
        }
        collection.insertMany(documents);
    }

    private String httpGet(String _url) throws ForbiddenException, SocketTimeoutException, IOException {
        BufferedReader rd;
        StringBuilder sb = new StringBuilder();
        String line;
        HttpURLConnection con;
        try {
            URL url = new URL(_url);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.setConnectTimeout(2000);
            con.connect();
        } catch (SocketTimeoutException e) {
            throw new SocketTimeoutException();
        }

        String encoding = con.getContentEncoding();
        // if Forbidden, key is dead
        if (con.getResponseCode() == 403) {
            throw new ForbiddenException("Key is dead");
        } else if (encoding != null && encoding.equals("gzip")) {
            rd = new BufferedReader(new InputStreamReader(new GZIPInputStream(con.getInputStream())));
        } else {
            rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
        }

        while ((line = rd.readLine()) != null) {
            sb.append(line).append('\n');
        }
        con.disconnect();

        return sb.toString();
    }

    private final Callable<Void> consumer = () -> {
        ArrayList<JsonObject> itemsToInsert = new ArrayList<>();

        // Replace the uri string with your MongoDB deployment's connection string
        String uri = "mongodb://localhost:27017";
        MongoClient mongoClient;
        MongoDatabase database;
        MongoCollection<Document> collection;
        try {
            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase("skygate");
            collection = database.getCollection("items");
        } catch (Exception e) {
            System.err.println("Error while connecting to MongoDB");
            e.printStackTrace();
            return null;
        }

        while (isRunning) {
            try {
                JsonObject item = items.poll(1, TimeUnit.SECONDS);
                if (item == null) {
                    continue;
                }
                itemsToInsert.add(item);
                if (itemsToInsert.size() == MONGO_BATCH_SIZE) {
                    //TODO: MONGO REQUEST

                    System.out.println(
                            "[LOG] Inserted " + itemsToInsert.size() +
                                    " items, " + items.size() +
                                    " items left, " + uuids.size() + " uuids left"
                    );
                    insertInMongo(collection, itemsToInsert);
                    itemsToInsert.clear();
                }
            } catch (Exception e) {
                System.err.println("Error while getting an item");
                e.printStackTrace();
            }
        }
        
        insertInMongo(collection, itemsToInsert);
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

            String response;
            try {
                response = httpGet("https://api.hypixel.net/skyblock/profiles?key=" + key +"&uuid=" + uuid);
            } catch (java.net.SocketTimeoutException e) {
                System.err.println("[ERROR] Socket timeout, adding back to the queue");
                addUuidToQueue(uuid);
                continue;
            } catch (java.io.IOException e) {
                System.err.println("[ERROR] " + e.toString() );
                continue;
            } catch (ForbiddenException e) {
                System.err.println("[ERROR] Key is dead, adding back to the queue");

                //TODO: remove key from keyToUsage ??
                addUuidToQueue(uuid);
                continue;
            } catch (Exception e) {
                System.err.println("[ERROR] Exception while getting the content, skipping uuid");
                e.printStackTrace();
                continue;
            }


            System.out.println("[LOG] Got response from API for uuid: " + uuid);

            JsonObject profilesEndpointJson;
            try {
                profilesEndpointJson = gson.fromJson(response, JsonObject.class);
            } catch (Exception e) {
                System.err.println("[ERROR] Exception while parsing the json");
                e.printStackTrace();
                continue;
            }

            ArrayList<JsonObject> profilesItems = searcher.getProfilesItems(profilesEndpointJson);

            if (profilesItems == null) {
                continue;
            }

            items.addAll(profilesItems);
        }

        System.out.println("[LOG] Producer stopped");
        return null;
    };

    private final Callable<Void> keyloop = () -> {

        while (ProducerConsumer.isRunning) {
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (Map.Entry<String, Integer> entry : keyToUsage.entrySet()) {
                keyToUsage.replace(entry.getKey(), 0);
            }
        }

        System.out.println("[LOG] Api keys loop stopped");
        return null;
    };

    public void run() throws InterruptedException {
        var pool = Executors.newCachedThreadPool();
        for (int i = 0; i < THREADS; i++) {
            pool.submit(producer);
        }
        pool.submit(consumer);
        pool.submit(keyloop);
        pool.shutdown();
        //pool.awaitTermination(forHowLong, unit);
    }
}

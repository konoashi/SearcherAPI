package fr.konoashi.searcher;

import static fr.konoashi.searcher.Base64.b64ToNbtCompound;
import static fr.konoashi.searcher.Utils.nbtCompoundToString;

import me.nullicorn.nedit.type.NBTCompound;
import com.google.gson.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.*;


public class Searcher {

    private static final Gson gson = new Gson();

    public static ArrayList<JsonObject> getProfilesItems(JsonObject profilesJson) {
        if (profilesJson == null || profilesJson.isJsonNull() || !profilesJson.has("profiles") || profilesJson.get("profiles").isJsonNull()) {
            return null;
        }
        JsonArray profiles = profilesJson.getAsJsonArray("profiles");
        if (profiles == null) {
            return null;
        }

        ArrayList<JsonObject> items = new ArrayList<>();
        for (JsonElement profileElement : profiles) {
            JsonObject profileJson = profileElement.getAsJsonObject();
            ArrayList<JsonObject> profileItems = getProfileItems(profileJson);
            items.addAll(profileItems);
        }

        return items;
    }

    public static ArrayList<JsonObject> getProfileItems(JsonObject profileJson) {

        String profileUuid = profileJson.get("profile_id").getAsString();
//        String cuteName = profileJson.get("cute_name").getAsString();

        ArrayList<JsonObject> items = new ArrayList<>();
        for (Entry<String, JsonElement> memberEntry : profileJson.getAsJsonObject("members").entrySet()) {
            String memberUuid = memberEntry.getKey();
            JsonObject memberJson = memberEntry.getValue().getAsJsonObject();
            ArrayList<JsonObject> profileItems = getMemberItems(memberJson, memberUuid, profileUuid);
            items.addAll(profileItems);
        }

        return items;
    }

    public static ArrayList<JsonObject> getMemberItems(JsonObject playerJson, String playerUuid, String profileUuid) {

        ArrayList<JsonObject> items = new ArrayList<>();

        // get pets
        JsonArray petsJson = playerJson.getAsJsonArray("pets");
        if (petsJson != null) {
            items.addAll(getPets(petsJson, playerUuid, profileUuid, "pets"));
        }

        // get inventory
        JsonObject inventoryJson = playerJson.getAsJsonObject("inv_contents");
        if (inventoryJson != null) {
            items.addAll(getInventory(inventoryJson, playerUuid, profileUuid, "inv"));
        }

        // get enderchest
        JsonObject enderChestJson = playerJson.getAsJsonObject("ender_chest_contents");
        if (enderChestJson != null) {
            items.addAll(getInventory(enderChestJson, playerUuid, profileUuid, "ender_chest"));
        }

        // get wardrobe
        JsonObject wardrobeJson = playerJson.getAsJsonObject("wardrobe_contents");
        if (wardrobeJson != null) {
            items.addAll(getInventory(wardrobeJson, playerUuid, profileUuid, "wardrobe"));
        }

        // get storage backpack
        JsonObject backpackJson = playerJson.getAsJsonObject("backpack_contents");
        if (backpackJson != null) {
            items.addAll(getMultipleInventories(backpackJson, playerUuid, profileUuid, "backpack"));
        }

        // get vault
        JsonObject vaultJson = playerJson.getAsJsonObject("personal_vault_contents");
        if (vaultJson != null) {
            items.addAll(getInventory(vaultJson, playerUuid, profileUuid, "vault"));
        }

//        System.out.println("[LOG] Gathered " + items.size() + " items from " + playerUuid + " (" + profileUuid + ")");
        return items;
    }

    private static ArrayList<JsonObject> getPets(JsonArray petsJson, String playerUuid, String profileUuid, String location) {
        ArrayList<JsonObject> pets = new ArrayList<>();

        int slot = 0;
        for (JsonElement petElement : petsJson.getAsJsonArray()) {
            JsonObject petJson = petElement.getAsJsonObject();
            JsonObject formattedPetJson = handleItem(petJson, playerUuid, profileUuid, location, slot++);
            pets.add(formattedPetJson);
        }

        return pets;
    }

    private static ArrayList<JsonObject> getInventory(JsonObject inventoryJson, String playerUuid, String profileUuid, String location) {
        ArrayList<JsonObject> items = new ArrayList<>();

        String nbt64 = inventoryJson.get("data").getAsString();

        try {
            NBTCompound nbtCompound = b64ToNbtCompound(nbt64);
            String nbtJsonString = nbtCompoundToString(nbtCompound);

            //TODO: fix error parsing pet json
            JsonArray nbtJson = gson.fromJson(nbtJsonString, JsonObject.class).getAsJsonArray("i");

            int slot = 0;
            for (JsonElement itemElement : nbtJson) {
                JsonObject itemJson = itemElement.getAsJsonObject();
                if (itemJson.entrySet().size() == 0) {
                    continue;
                }
                JsonObject formattedItemJson = handleItem(itemJson, playerUuid, profileUuid, location, slot++);
                items.add(formattedItemJson);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    private static ArrayList<JsonObject> getMultipleInventories(JsonObject backpackJson, String playerUuid, String profileUuid, String location) {
        ArrayList<JsonObject> items = new ArrayList<>();

        if (backpackJson == null || backpackJson.isJsonNull()) {
            return items;
        }

        for (int i = 0; i < backpackJson.keySet().size(); i++) {
            JsonObject inventoryJson = backpackJson.getAsJsonObject(Integer.toString(i));
            if (inventoryJson == null || inventoryJson.isJsonNull()) {
                continue;
            }
            String _location = location + " " + (i+1);
            items.addAll(getInventory(inventoryJson, playerUuid, profileUuid, _location));
        }

        return items;
    }

    private static JsonObject handleItem(JsonObject itemJson, String playerUuid, String profileUuid, String container, int slot) {

        if (
                itemJson.getAsJsonObject("tag") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("id") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("id").getAsString().equals("PET")
        ) {
            parsePetInfo(itemJson);
        }

        return addProvenance(itemJson, playerUuid, profileUuid, container, slot);
    }

    private static void parsePetInfo(JsonObject itemJson) {
        String petInfoString = itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("petInfo").getAsString();
        JsonObject petInfoJson = gson.fromJson(petInfoString, JsonObject.class);
        itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").add("petInfo", petInfoJson);
    }

    private static JsonObject addProvenance(JsonObject item, String playerUuid, String profileUuid, String container, int slot) {
        JsonObject provenance = new JsonObject();
        provenance.addProperty("player_uuid", playerUuid);
        provenance.addProperty("profile_uuid", profileUuid);
        provenance.addProperty("container", container);
        provenance.addProperty("slot", slot);
        provenance.add("item", item);

        return provenance;
    }
}

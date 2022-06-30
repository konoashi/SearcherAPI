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

//        // get inventory
//        JsonObject inventoryJson = playerJson.getAsJsonObject("inv_contents");
//        if (inventoryJson != null) {
//            items.addAll(getInventory(inventoryJson, playerUuid, profileUuid, "inv"));
//        }
//
//        // get enderchest
//        JsonObject enderChestJson = playerJson.getAsJsonObject("ender_chest_contents");
//        if (enderChestJson != null) {
//            items.addAll(getInventory(enderChestJson, playerUuid, profileUuid, "ender_chest"));
//        }
//
//        // get wardrobe
//        JsonObject wardrobeJson = playerJson.getAsJsonObject("wardrobe_contents");
//        if (wardrobeJson != null) {
//            items.addAll(getInventory(wardrobeJson, playerUuid, profileUuid, "wardrobe"));
//        }
//
//        // get storage backpack
//        JsonObject backpackJson = playerJson.getAsJsonObject("backpack_contents");
//        if (backpackJson != null) {
//            items.addAll(getMultipleInventories(backpackJson, playerUuid, profileUuid, "backpack"));
//        }
//
//        // get vault
//        JsonObject vaultJson = playerJson.getAsJsonObject("personal_vault_contents");
//        if (vaultJson != null) {
//            items.addAll(getInventory(vaultJson, playerUuid, profileUuid, "vault"));
//        }

//        System.out.println("[LOG] Gathered " + items.size() + " items from " + playerUuid + " (" + profileUuid + ")");
        return items;
    }

    private static ArrayList<JsonObject> getPets(JsonArray petsJson, String playerUuid, String profileUuid, String location) {
        ArrayList<JsonObject> pets = new ArrayList<>();

        for (JsonElement petElement : petsJson.getAsJsonArray()) {
            JsonObject petJson = petElement.getAsJsonObject();
            JsonObject formattedPetJson = handleItem(petJson, playerUuid, profileUuid, location);
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

            for (JsonElement itemElement : nbtJson) {
                JsonObject itemJson = itemElement.getAsJsonObject();
                if (itemJson.entrySet().size() == 0) {
                    continue;
                }
                JsonObject formattedItemJson = handleItem(itemJson, playerUuid, profileUuid, location);
                items.add(formattedItemJson);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    private static ArrayList<JsonObject> getMultipleInventories(JsonObject backpackJson, String playerUuid, String profileUuid, String location) {
        ArrayList<JsonObject> items = new ArrayList<>();

        for (int i = 0; i < backpackJson.size(); i++) {
            JsonObject inventoryJson = backpackJson.getAsJsonObject(Integer.toString(i));

            String _location = location + " " + (i+1);
            items.addAll(getInventory(inventoryJson, playerUuid, profileUuid, _location));

        }

        return items;
    }

    private static JsonObject handleItem(JsonObject itemJson, String playerUuid, String profileUuid, String container) {

        if (
                itemJson.getAsJsonObject("tag") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("id") != null &&
                itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("id").getAsString().equals("PET")
        ) {
            parsePetInfo(itemJson);
        }

        return addProvenance(itemJson, playerUuid, profileUuid, container);
    }

    private static void parsePetInfo(JsonObject itemJson) {
        String petInfoString = itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").get("petInfo").getAsString();
        JsonObject petInfoJson = gson.fromJson(petInfoString, JsonObject.class);
        itemJson.getAsJsonObject("tag").getAsJsonObject("ExtraAttributes").add("petInfo", petInfoJson);
    }

    private static JsonObject addProvenance(JsonObject item, String playerUuid, String profileUuid, String container) {
        JsonObject provenance = new JsonObject();
        provenance.addProperty("player_uuid", playerUuid);
        provenance.addProperty("profile_uuid", profileUuid);
        provenance.addProperty("container", container);
        provenance.add("item", item);

        return provenance;
    }

//    private static NBTCompound petsNbtDispatch(NBTCompound nbtlist) {
//
//        NBTList itemsList = nbtlist.getList("i");
//        for (int i = 0; i < itemsList.size(); i++) {
//            if (itemsList.getCompound(i).keySet().size() == 0) {
//                continue;
//            }
//            if (Objects.equals(itemsList.getCompound(i).getCompound("tag").getCompound("ExtraAttributes").get("id").toString(), "PET")) {
//                //itemsList.removeIf(); peut etre utile mais je sais pas comment l'utiliser
//                itemsList.remove(i);
//            }
//        }
//
//        return nbtlist;
//    }

//    private static JsonArray getPetsJson(NBTCompound nbtlist) {
//
//        NBTList itemsList = nbtlist.getList("i");
//        JsonArray pets = new JsonArray();
//        for (int i = 0; i < itemsList.size(); i++) {
//            if (itemsList.getCompound(i).keySet().size() == 0) {
//                continue;
//            }
//            if (Objects.equals(itemsList.getCompound(i).getCompound("tag").getCompound("ExtraAttributes").get("id").toString(), "PET")) {
//                pets.add(new JsonParser().parse(itemsList.getCompound(i).getCompound("tag").getCompound("ExtraAttributes").get("petInfo").toString()).getAsJsonObject());
//            }
//        }
//
//        return pets;
//    }

//    private void WARDROBE(JsonElement data, String profile, UUID uuid) {
//        String nbt64 = data.getAsJsonObject().get("data").getAsString();
//        NBTList nbtlist= new NBTList(TagType.STRING);
//        try {
//            nbtlist = BASE64TOJSON(nbt64);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        for (int i = 0; i < nbtlist.size(); i++) {
//            if(nbtlist.getCompound(i).getCompound("tag") != null) {
//                EXOTIC(nbtlist.get(i), profile, uuid, "Wardrobe");
//                String displayname = nbtlist.getCompound(i).getCompound("tag").getCompound("display").getString("Name");
//                String name = nbtlist.getCompound(i).getCompound("tag").getCompound("ExtraAttributes").getString("id");
//
//                if (Objects.equals(name, "HAUNT_ABILITY")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Haunt");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "WATCHER_EYE")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Watcher Eye");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "TANK_DUNGEON_ABILITY_1")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Stun Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "TANK_DUNGEON_ABILITY_2")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Absorption Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "HEALER_DUNGEON_ABILITY_2")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Healing Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "HEALER_DUNGEON_ABILITY_3")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Revive Self");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "WARRIOR_DUNGEON_ABILITY_1")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Strength Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("Catacombs")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Catacombs Pass");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Wardrobe");
//                    datacheck.add(prop);
//                }
//                if (name.equals("PET")) {
//                    String petInfo = nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(0, 11) + nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(12, 189) + nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(190);
//                    String type = new JsonParser().parse(petInfo).getAsJsonObject().getAsJsonObject("petInfo").get("type").getAsString();
//                    String tier = new JsonParser().parse(petInfo).getAsJsonObject().getAsJsonObject("petInfo").get("tier").getAsString();
//
//                    if (Objects.equals(tier, "RARE")) {
//                        if (Objects.equals(type, "PIGMAN")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Pigman PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Wardrobe");
//                            datacheck.add(prop);
//                        }
//                        if (Objects.equals(type, "BLAZE")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Blaze PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Wardrobe");
//                            datacheck.add(prop);
//                        }
//                        if (Objects.equals(type, "WITHER_SKELETON")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Wither Skeleton PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Wardrobe");
//                            datacheck.add(prop);
//                        }
//
//                    }
//
//                    if (Objects.equals(tier, "COMMON")) {
//                        if (Objects.equals(type, "SNOWMAN")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Common Snowman PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Wardrobe");
//                            datacheck.add(prop);
//                        }
//                    }
//
//                    if (displayname.contains("Mystery")) {
//                        JsonObject prop = new JsonObject();
//                        prop.addProperty("Found", "Mystery Pet");
//                        prop.addProperty("UUID", String.valueOf(uuid));
//                        prop.addProperty("Profile", profile);
//                        prop.addProperty("Location", "Wardrobe");
//                        datacheck.add(prop);
//                    }
//
//                    if (displayname.contains("Unknown")) {
//                        JsonObject prop = new JsonObject();
//                        prop.addProperty("Found", "Unknown Pet");
//                        prop.addProperty("UUID", String.valueOf(uuid));
//                        prop.addProperty("Profile", profile);
//                        prop.addProperty("Location", "Wardrobe");
//                        datacheck.add(prop);
//                    }
//
//
//
//                }
//            }
//
//        }
//
//
//    }
//    private void INVENTORY(JsonElement data, String profile, UUID uuid) {
//        String nbt64 = data.getAsJsonObject().get("data").getAsString();
//        NBTList nbtlist= new NBTList(TagType.STRING);
//        try {
//            nbtlist = BASE64TOJSON(nbt64);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (int i = 0; i < nbtlist.size(); i++) {
//            if (nbtlist.getCompound(i).getCompound("tag") != null) {
//                EXOTIC(nbtlist.get(i), profile, uuid, "Inventory");
//                String displayname = nbtlist.getCompound(i).getCompound("tag").getCompound("display").getString("Name");
//                String name = nbtlist.getCompound(i).getCompound("tag").getCompound("ExtraAttributes").getString("id");
//
//                if (Objects.equals(name, "HAUNT_ABILITY")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Haunt");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "WATCHER_EYE")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Watcher Eye");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "TANK_DUNGEON_ABILITY_1")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Stun Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "TANK_DUNGEON_ABILITY_2")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Absorption Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "HEALER_DUNGEON_ABILITY_2")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Healing Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "HEALER_DUNGEON_ABILITY_3")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Revive Self");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "WARRIOR_DUNGEON_ABILITY_1")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Strength Potion");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "ARCHER_DUNGEON_ABILITY_3")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Healing Bow");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "ARCHER_DUNGEON_ABILITY_2")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Stun Bow");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "BOSS_SPIRIT_BOW")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Spirit Bow");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "MAGE_DUNGEON_ABILITY_3")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Fireball");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "GHOST_THROWING_AXE")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Ghost Axe");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "MAGE_DUNGEON_ABILITY_1")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Pop-up Wall");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (Objects.equals(name, "")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//
//
//                if (displayname.contains("Catacombs")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Catacombs Pass");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("Phone Number")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "Maddox Phone Number");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//                if (displayname.contains("")) {
//                    JsonObject prop = new JsonObject();
//                    prop.addProperty("Found", "");
//                    prop.addProperty("UUID", String.valueOf(uuid));
//                    prop.addProperty("Profile", profile);
//                    prop.addProperty("Location", "Inventory");
//                    datacheck.add(prop);
//                }
//
//
//
//
//
//
//                if (name.equals("PET")) {
//                    String petInfo = nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(0, 11) + nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(12, 189) + nbtlist.getCompound(i).getCompound("tag").get("ExtraAttributes").toString().replace("petInfo", "\"petInfo\"").substring(190);
//                    String type = new JsonParser().parse(petInfo).getAsJsonObject().getAsJsonObject("petInfo").get("type").getAsString();
//                    String tier = new JsonParser().parse(petInfo).getAsJsonObject().getAsJsonObject("petInfo").get("tier").getAsString();
//
//                    if (Objects.equals(tier, "RARE")) {
//                        if (Objects.equals(type, "PIGMAN")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Pigman PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Inventory");
//                            datacheck.add(prop);
//                        }
//                        if (Objects.equals(type, "BLAZE")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Blaze PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Inventory");
//                            datacheck.add(prop);
//                        }
//                        if (Objects.equals(type, "WITHER_SKELETON")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Rare Wither Skeleton PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Inventory");
//                            datacheck.add(prop);
//                        }
//
//                    }
//
//                    if (Objects.equals(tier, "COMMON")) {
//                        if (Objects.equals(type, "SNOWMAN")) {
//                            JsonObject prop = new JsonObject();
//                            prop.addProperty("Found", "Common Snowman PET");
//                            prop.addProperty("UUID", String.valueOf(uuid));
//                            prop.addProperty("Profile", profile);
//                            prop.addProperty("Location", "Inventory");
//                            datacheck.add(prop);
//                        }
//                    }
//
//                    if (displayname.contains("Mystery")) {
//                        JsonObject prop = new JsonObject();
//                        prop.addProperty("Found", "Mystery Pet");
//                        prop.addProperty("UUID", String.valueOf(uuid));
//                        prop.addProperty("Profile", profile);
//                        prop.addProperty("Location", "Inventory");
//                        datacheck.add(prop);
//                    }
//
//                    if (displayname.contains("Unknown")) {
//                        JsonObject prop = new JsonObject();
//                        prop.addProperty("Found", "Unknown Pet");
//                        prop.addProperty("UUID", String.valueOf(uuid));
//                        prop.addProperty("Profile", profile);
//                        prop.addProperty("Location", "Wardrobe");
//                        datacheck.add(prop);
//                    }
//
//
//
//                }
//
//            }
//        }
//
//    }
//    private void ENDERCHEST(JsonElement data, String profile, UUID uuid) {
//        String nbt64 = data.getAsJsonObject().get("data").getAsString();
//        NBTList nbtlist= new NBTList(TagType.STRING);
//        try {
//            nbtlist = BASE64TOJSON(nbt64);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//    private void STORAGE(JsonElement data, String profile, UUID uuid) {
//        ArrayList<String> backpacksnbt64 = new ArrayList<>();
//        for (int i = 0; i < data.getAsJsonObject().size(); i++) {
//            backpacksnbt64.add(data.getAsJsonObject().get(Integer.toString(i)).getAsJsonObject().get("data").getAsString());
//        }
//        ArrayList<NBTList> nbtlist = new ArrayList<>();
//        try {
//            for (int i = 0; i < backpacksnbt64.size(); i++) {
//                nbtlist.add(BASE64TOJSON(backpacksnbt64.get(i)));
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//    private void VAULT(JsonElement data, String profile, UUID uuid){
//        String nbt64 = data.getAsJsonObject().get("data").getAsString();
//        NBTList nbtlist= new NBTList(TagType.STRING);
//        try {
//            nbtlist = BASE64TOJSON(nbt64);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void EXOTIC(Object data, String profile, UUID uuid, String from) {
//
//    }
}

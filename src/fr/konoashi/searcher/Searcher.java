package fr.konoashi.searcher;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nullicorn.nedit.type.NBTCompound;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.*;

import static fr.konoashi.searcher.Base64.b64ToNbtCompound;

public class Searcher {

    private static final Gson gson = new Gson();

    public static ArrayList<JsonObject> getProfilesItems(JsonObject profilesJson) {
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
            items.addAll(getPets(petsJson, playerUuid, profileUuid));
        }

        // get inventory
        JsonObject inventoryJson = playerJson.getAsJsonObject("inv_contents");
        if (inventoryJson != null) {
            items.addAll(getInventory(inventoryJson, playerUuid, profileUuid));
        }

        return items;
    }

//    private void CheckStuff(JsonElement data, String uuid) {
//        for (int i = 0; i < data.getAsJsonObject().getAsJsonArray("profiles").size(); i++) {
//            JsonElement profile = data.getAsJsonObject().getAsJsonArray("profiles").get(i);
//            JsonElement member = profile.getAsJsonObject().get("members").getAsJsonObject().get(uuid.replaceAll("-", ""));
//
//
//            if(member.getAsJsonObject().getAsJsonArray("pets") != null) {
//                getPets(member.getAsJsonObject().getAsJsonArray("pets"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//            if(member.getAsJsonObject().get("wardrobe_contents") != null) {
//                WARDROBE(member.getAsJsonObject().get("wardrobe_contents"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//            if(member.getAsJsonObject().get("inv_contents") != null) {
//                INVENTORY(member.getAsJsonObject().get("inv_contents"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//            if(member.getAsJsonObject().get("ender_chest_contents") != null) {
//                ENDERCHEST(member.getAsJsonObject().get("ender_chest_contents"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//            if(member.getAsJsonObject().get("backpack_contents") != null) {
//                STORAGE(member.getAsJsonObject().get("backpack_contents"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//            if(member.getAsJsonObject().get("personal_vault_contents") != null) {
//                VAULT(member.getAsJsonObject().get("personal_vault_contents"), profile.getAsJsonObject().get("cute_name").getAsString(), UUID.fromString(uuid));
//            }
//
//
//            //datacheck.add(new JsonParser().parse(String.valueOf(sb)));
//        }
//    }
    private static ArrayList<JsonObject> getPets(JsonArray petsJson, String playerUuid, String profileUuid) {
        ArrayList<JsonObject> pets = new ArrayList<>();

        for (JsonElement petElement : petsJson.getAsJsonArray()) {

            JsonObject petJson = petElement.getAsJsonObject();
            JsonObject formattedPetJson = addProvenance(petJson, playerUuid, profileUuid, "pets");

            pets.add(formattedPetJson);
        }

        return pets;
    }

    private static ArrayList<JsonObject> getInventory(JsonObject inventoryJson, String playerUuid, String profileUuid) {
        ArrayList<JsonObject> items = new ArrayList<>();

        String nbt64 = inventoryJson.get("data").getAsString();

        try {
            NBTCompound nbtlist = b64ToNbtCompound(nbt64);
            String nbtJsonString = nbtlist.toString();
            JsonArray nbtJson = gson.fromJson(nbtJsonString, JsonObject.class).getAsJsonArray("i");

            for (JsonElement itemElement : nbtJson) {
                JsonObject itemJson = itemElement.getAsJsonObject();
                if (itemJson.entrySet().size() == 0) {
                    continue;
                }

                JsonObject formattedItemJson = addProvenance(itemJson, playerUuid, profileUuid, "inventory");
                items.add(formattedItemJson);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    private static JsonObject addProvenance(JsonObject item, String playerUuid, String profileUuid, String container) {
        JsonObject provenance = new JsonObject();
        provenance.addProperty("player_uuid", playerUuid);
        provenance.addProperty("profile_uuid", profileUuid);
        provenance.addProperty("container", container);
        provenance.add("item", item);

        return provenance;
    }
//
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

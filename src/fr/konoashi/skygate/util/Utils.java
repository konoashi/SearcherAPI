package fr.konoashi.skygate.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Utils {

    public static final Gson gson = new Gson();

    public static String[] excludeFromItemsApi= {"WARRIOR_DUNGEON_ABILITY_1", "HEALER_DUNGEON_ABILITY_3", "ARCHER_DUNGEON_ABILITY_2", "TANK_DUNGEON_ABILITY_1", "TANK_DUNGEON_ABILITY_2", "MAGE_DUNGEON_ABILITY_2", "HEALER_DUNGEON_ABILITY_1", "ARCHER_DUNGEON_ABILITY_3", "MAGE_DUNGEON_ABILITY_1", "ARCHER_DUNGEON_ABILITY_1", "HEALER_DUNGEON_ABILITY_2", "MAGE_DUNGEON_ABILITY_3", "HAUNT_ABILITY", "SECRET_DUNGEON_REDSTONE_KEY", "DUNGEON_WIZARD_CRYSTAL", "GEMSTONE_COLLECTION", "MUSHROOM_COLLECTION", "DEAD_BUSH_OF_LOVE", "CREATIVE_MIND", "KLOONBOAT", "ANCIENT_ELEVATOR", "GAME_BREAKER", "GAME_ANNIHILATOR", "GAME_FIXER", "MEGA_LUCK", "WIKI_JOURNAL", "NOVA_SWORD", "STAR_LEGGINGS", "QUALITY_MAP", "ZOOM_PICKAXE", "STAR_SWORD_3000", "STAR_CHESTPLATE", "DUECES_BUILDER_CLAY", "STAR_SWORD_9000", "SHINY_RELIC", "PILE_OF_CASH", "SPELL_BOOK_TEST", "THE_WATCHERS_HEAD", "STAR_BOOTS", "RAYGUN", "STAR_HELMET", "WATCHER_EYE", "FEROCITY_SWORD_150", "GEMSTONE_POWDER_TIER_1", "GEMSTONE_POWDER_TIER_2", "GEMSTONE_POWDER_TIER_3", "MITHRIL_POWDER_TIER_1", "MITHRIL_POWDER_TIER_2", "MITHRIL_POWDER_TIER_3", "DRAGON_EGG", "IRON_BARDING", "DIAMOND_BARDING", "GOLD_BARDING"};

    private static final String petsExcludeJson = "{\"pets\":[\n" +
            "  {\n" +
            "   \"name\": \"BAT\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\", \"MYTHIC\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ENDERMAN\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\", \"MYTHIC\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"JERRY\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\", \"MYTHIC\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"FLYING_FISH\", \n" +
            "   \"rarities\": [\"RARE\", \"EPIC\", \"LEGENDARY\", \"MYTHIC\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ARMADILLO\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BEE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BINGO\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BLUE_WHALE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"CHICKEN\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"DOLPHIN\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ELEPHANT\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ENDERMITE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GIRAFFE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GRANDMA_WOLF\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GRIFFIN\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GUARDIAN\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"HORSE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"KUUDRA\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"LION\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"MAGMA_CUBE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"MITHRIL_GOLEM\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"MONKEY\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"MOOSHROOM_COW\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"OCELOT\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"PIG\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"RABBIT\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ROCK\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SHEEP\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SILVERFISH\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SKELETON\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SNAIL\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SPIDER\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SQUID\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"TIGER\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"WOLF\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ZOMBIE\", \n" +
            "   \"rarities\": [\"COMMON\", \"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"DROPLET_WISP\", \n" +
            "   \"rarities\": [\"UNCOMMON\", \"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SCATHA\", \n" +
            "   \"rarities\": [\"RARE\", \"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BABY_YETI\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BAL\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BLAZE\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"ENDER_DRAGON\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GHOUL\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GOLEM\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"HOUND\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"JELLYFISH\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"MEGALODON\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"PARROT\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"PHOENIX\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"PIGMAN\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SPIRIT\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"TARANTULA\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"TURTLE\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"WITHER_SKELETON\", \n" +
            "   \"rarities\": [\"EPIC\", \"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"AMMONITE\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"BLACK_CAT\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"GOLDEN_DRAGON\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"RAT\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SKELETON_HORSE\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  },\n" +
            "  {\n" +
            "   \"name\": \"SNOWMAN\", \n" +
            "   \"rarities\": [\"LEGENDARY\"]\n" +
            "  }\n" +
            "  \n" +
            "]}";

    final public static JsonObject petExclude = gson.fromJson(petsExcludeJson, JsonObject.class);

    public static String decompress(final byte[] compressed) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(compressed);
        GZIPInputStream gis = new GZIPInputStream(bis);
        byte[] bytes = IOUtils.toByteArray(gis);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String nbtCompoundToString(NBTCompound nbt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;

        for(Iterator<Map.Entry<String, Object>> var3 = nbt.entrySet().iterator(); var3.hasNext(); ++i) {
            Map.Entry<String, Object> tag = (Map.Entry<String, Object>) var3.next();
            if (i != 0) {
                sb.append(",");
            }

            sb.append(((String)tag.getKey()).isEmpty() ? "\"\"" : "\"" + (String)tag.getKey() + "\"");
            sb.append(":");
            sb.append(tagToString(tag.getValue()));
        }

        sb.append("}");
        return sb.toString();
    }

    private static String tagToString(Object value) {
        TagType tagType = TagType.fromObject(value);
        return switch (tagType) {
            case BYTE -> byteToString((Byte) value);
            case SHORT -> shortToString((Short) value);
            case INT -> intToString((Integer) value);
            case LONG -> longToString((Long) value);
            case FLOAT -> floatToString((Float) value);
            case DOUBLE -> doubleToString((Double) value);
            case BYTE_ARRAY -> byteArrayToString((byte[]) value);
            case STRING -> stringTagToString((String) value);
            case LIST -> listToString((NBTList) value);
            case COMPOUND -> nbtCompoundToString((NBTCompound) value);
            case INT_ARRAY -> intArrayToString((int[]) value);
            case LONG_ARRAY -> longArrayToString((long[]) value);
            default -> "";
        };
    }

    private static String byteToString(byte value) {
        return value + "";
    }

    private static String shortToString(short value) {
        return value + "";
    }

    private static String intToString(int value) {
        return Integer.toString(value);
    }

    private static String longToString(long value) {
        return value + "";
    }

    private static String floatToString(float value) {
        return value + "";
    }

    private static String doubleToString(double value) {
        return value + "";
    }

    private static String byteArrayToString(byte[] value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for(int i = 0; i < value.length; ++i) {
            sb.append(value[i]);
            if (i < value.length - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static String stringTagToString(String value) {
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }

    private static String listToString(NBTList value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for(int i = 0; i < value.size(); ++i) {
            sb.append(value.get(i) != null ? tagToString(value.get(i)) : 0);
            if (i < value.size() - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static String intArrayToString(int[] value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for(int i = 0; i < value.length; ++i) {
            sb.append(value[i]);
            if (i < value.length - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static String longArrayToString(long[] value) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for(int i = 0; i < value.length; ++i) {
            sb.append(value[i]);
            if (i < value.length - 1) {
                sb.append(",");
            }
        }

        sb.append("]");
        return sb.toString();
    }
}

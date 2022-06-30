package fr.konoashi.searcher;

import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;

import java.util.Iterator;
import java.util.Map;

public class Utils {

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

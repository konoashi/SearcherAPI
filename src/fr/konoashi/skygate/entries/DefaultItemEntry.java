package fr.konoashi.skygate.entries;

public class DefaultItemEntry {
    public String name;
    public String id;
    public String tier;
    public String material;
    public String color;

    public DefaultItemEntry(String material, String name,  String tier, String id, String color) {
        this.material = material;
        this.name = name;
        this.id = id;
        this.tier = tier;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getTier() {
        return tier;
    }

    public String getMaterial() {
        return material;
    }

    public String getColor() {
        if (!hasColor()) {
            return null;
        }
        return color;
    }

    public boolean hasColor() {
        return color != null;
    }

    public String toString() {
        return name + " (" + id + ")" + " [" + color + "]";
    }
}

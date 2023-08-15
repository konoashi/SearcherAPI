package fr.konoashi.skygate.entries;

public class DefaultPetEntry {
    public String type;
    public String[] tier;

    public DefaultPetEntry(String[] tier, String type) {
        this.tier = tier;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String[] getTier() {
        return tier;
    }

}

package com.ratherbeembed.pokemonhintsolver;

import java.util.List;
import java.util.Map;

public class Pokemon {
    private String Name;
    private int Number;
    private String Description;
    private String Evolution;
    private String Rarity;
    private List<String> Types;
    private String Region;
    private boolean Catchable;
    private Map<String, List<String>> Names;
    private Map<String, Integer> BaseStats;
    private Map<String, String> Appearance;
    private String URL;

    public Pokemon(String name, String url) {
    }

    // Add getters and setters for the fields

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getEvolution() {
        return Evolution;
    }

    public void setEvolution(String evolution) {
        Evolution = evolution;
    }

    public String getRarity() {
        return Rarity;
    }

    public void setRarity(String rarity) {
        Rarity = rarity;
    }

    public List<String> getTypes() {
        return Types;
    }

    public void setTypes(List<String> types) {
        Types = types;
    }

    public String getRegion() {
        return Region;
    }

    public void setRegion(String region) {
        Region = region;
    }

    public boolean isCatchable() {
        return Catchable;
    }

    public void setCatchable(boolean catchable) {
        Catchable = catchable;
    }

    public Map<String, List<String>> getNames() {
        return Names;
    }

    public void setNames(Map<String, List<String>> names) {
        Names = names;
    }

    public Map<String, Integer> getBaseStats() {
        return BaseStats;
    }

    public void setBaseStats(Map<String, Integer> baseStats) {
        BaseStats = baseStats;
    }

    public Map<String, String> getAppearance() {
        return Appearance;
    }

    public void setAppearance(Map<String, String> appearance) {
        Appearance = appearance;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}


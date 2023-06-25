package com.ratherbeembed.pokemonhintsolver;

public class SearchResult {
    private String prefix;
    private String url;

    public SearchResult(String prefix, String url) {
        this.prefix = prefix;
        this.url = url;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUrl() {
        return url;
    }

    public String getTextToCopy() {
        return prefix;
    }
}
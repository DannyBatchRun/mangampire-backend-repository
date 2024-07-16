package org.altervista.mangampire.model;

public class SearchRequest {
    private String name;
    private int volume;

    public SearchRequest() {

    }

    public SearchRequest(String name, int volume) {
        this.name = name;
        this.volume = volume;
    }

    public String getName() {
        return name;
    }

    public int getVolume() {
        return volume;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

}

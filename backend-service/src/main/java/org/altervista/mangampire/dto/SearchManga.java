package org.altervista.mangampire.dto;

import lombok.Data;

@Data
public class SearchManga {
    private String name;
    private int volume;

    public SearchManga() {

    }

    public SearchManga(String name, int volume) {
        this.name = name;
        this.volume = volume;
    }

}

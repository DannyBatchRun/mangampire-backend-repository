package org.altervista.mangampire.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

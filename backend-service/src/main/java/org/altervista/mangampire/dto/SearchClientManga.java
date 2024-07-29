package org.altervista.mangampire.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@ToString
public class SearchClientManga {

    private SearchClient client;
    private SearchManga manga;

    public SearchClientManga() {

    }

    public SearchClientManga(SearchClient client, SearchManga manga) {
        this.client = client;
        this.manga = manga;
    }
}

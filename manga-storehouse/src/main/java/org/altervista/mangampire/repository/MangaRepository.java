package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Manga;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface MangaRepository extends CrudRepository<Manga, Long> {
    Manga findByIdManga(long idManga);
    Manga findByNameAndVolume(String name, int volume);
}
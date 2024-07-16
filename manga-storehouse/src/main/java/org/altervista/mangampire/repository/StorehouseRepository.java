package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Manga;
import org.altervista.mangampire.model.Storehouse;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StorehouseRepository extends CrudRepository<Storehouse, Long> {
    Storehouse findByManga(Manga manga);
}

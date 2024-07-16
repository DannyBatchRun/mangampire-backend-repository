package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends CrudRepository<Card, Long> {
    Card findByCardNumber(String cardNumber);

}

package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Client;
import org.altervista.mangampire.model.ShoppingCart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingCartRepository extends CrudRepository<ShoppingCart, Long> {
    ShoppingCart findByIdClient(long idClient);

}

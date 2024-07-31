package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Client;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {
    Client findByIdClientAndEmailAndNameAndSurnameAndDateBirth(long idClient, String email, String name, String surname, String dateBirth);
    Client findByIdClientAndNameAndSurnameAndDateBirth(long idClient, String name, String surname, String dateBirth);
    Client findByNameAndSurnameAndDateBirth(String name, String surname, String dateBirth);
    Client findByEmail(String email);
    Client findByIdClient(long idClient);

}

package org.altervista.mangampire.repository;

import org.altervista.mangampire.model.Register;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterRepository extends CrudRepository<Register, Long> {
    Register findByPublisher(String publisher);
}

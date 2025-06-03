package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.electro.electro.entities.Person;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByEmail(String email);
}

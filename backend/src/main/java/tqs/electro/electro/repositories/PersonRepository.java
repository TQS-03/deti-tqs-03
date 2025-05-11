package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tqs.electro.electro.entities.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}

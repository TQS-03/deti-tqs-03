package tqs.electro.electro.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tqs.electro.electro.entities.Station;

import java.util.UUID;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    Station findById(UUID id);
}
